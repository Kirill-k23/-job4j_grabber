package ru.job4j.grabber;

import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class PsqlStore implements Store {
    private Connection connection;

    public PsqlStore(Properties config) {
        try {
            Class.forName(config.getProperty("jdbc.driver"));
            connection = DriverManager.getConnection(
                    config.getProperty("jdc.url"),
                    config.getProperty("jdc.username"),
                    config.getProperty("jdc.password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Post createPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getInt(1));
        post.setTitle(rs.getString(2));
        post.setDescription(rs.getString(3));
        post.setLink(rs.getString(4));
        post.setCreated(LocalDateTime.parse(rs.getString(5).replace(" ", "T")));
        return post;
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO post(name, text, link, created) VALUES (?, ?, ?, ?) ON CONFLICT (link) DO NOTHING"
        )) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getDescription());
            ps.setString(3, post.getLink());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> postList = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM post")) {
            ps.execute();
            try (ResultSet rs = ps.getResultSet()) {
                while (rs.next()) {
                    postList.add(createPost(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return postList;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM post WHERE id = ?")) {
            ps.setInt(1, id);
            ps.execute();
            try (ResultSet rs = ps.getResultSet()) {
                while (rs.next()) {
                    post = createPost(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) {
        Parse parser = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> posts = null;
        try {
            posts = parser.list("https://career.habr.com");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (InputStream in = PsqlStore.class.getClassLoader()
                .getResourceAsStream("store.properties")) {
            Properties prop = new Properties();
            prop.load(in);
            try (Store store = new PsqlStore(prop)) {
                Objects.requireNonNull(posts).forEach(store::save);
                List<Post> getPost = store.getAll();
                getPost.forEach(System.out::println);
                System.out.println(store.findById(10));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

