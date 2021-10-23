package com.travellerAPI.Traveller.Repositories;

import com.travellerAPI.Traveller.Models.Notification;
import com.travellerAPI.Traveller.Models.Subscription;
import com.travellerAPI.Traveller.Models.Topic;
import com.travellerAPI.Traveller.Models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserDataRepository implements IUserDataRepository{

    private static final Logger logger = LoggerFactory.getLogger(UserDataRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SimpleJdbcInsert simpleJdbcInsert;
    private SimpleJdbcInsert simpleJdbcInsertSubscription;
    private SimpleJdbcInsert simpleJdbcInsertTopic;
    private SimpleJdbcInsert simpleJdbcInsertNotification;

    UserDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource()).withTableName("user_table").usingGeneratedKeyColumns("user_id");
        this.simpleJdbcInsertSubscription = new SimpleJdbcInsert(jdbcTemplate.getDataSource()).withTableName("subscriber_table").usingGeneratedKeyColumns("subscriber_id");
        this.simpleJdbcInsertTopic = new SimpleJdbcInsert(jdbcTemplate.getDataSource()).withTableName("topic_details").usingGeneratedKeyColumns("label_id");
        this.simpleJdbcInsertNotification = new SimpleJdbcInsert(jdbcTemplate.getDataSource()).withTableName("notifications").usingGeneratedKeyColumns("notification_id");

    }

    @Override
    public User saveUser(User user) {
        logger.info("START UserDataRepository saveUser(User)");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("user_name", user.getName());
        Number id = simpleJdbcInsert.executeAndReturnKey(params);
        logger.info("Generated a new user with id:"+id.longValue());
        user.setId(id.longValue());
        logger.info("END UserDataRepository saveUser(User)");
        return user;
    }

    @Override
    public Subscription saveSubscription(Subscription subscription) {
        logger.info("START UserDataRepository saveSubscription(subscription)");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", subscription.getUserId());
        params.put("topic_name", subscription.getTopicName());
        Number id = simpleJdbcInsertSubscription.executeAndReturnKey(params);
        logger.info("Generated a new subscription with id:"+id.longValue());
        subscription.setId(id.longValue());
        logger.info("END UserDataRepository saveSubscription(subscription)");
        return subscription;
    }

    @Override
    public boolean saveLabel(String label) {
        logger.info("START UserDataRepository saveLabel(User)");
        Map<String, Object> params = new HashMap<>();
        params.put("label_id", (long)0);
        params.put("label", label);
        params.put("india", "");
        params.put("egypt", "");
        params.put("singapore", "");
        Number id = simpleJdbcInsertTopic.executeAndReturnKey(params);
        logger.info("Generated a new label with id:"+id.longValue());
        logger.info("END UserDataRepository saveLabel(User)");
        return id.longValue() > 0;
    }

    @Override
    public Notification addNotification(Notification notification) {
        logger.info("START UserDataRepository addNotification(notification)");
        Map<String, Object> params = new HashMap<>();
        params.put("status", notification.getStatus());
        params.put("topic_name", notification.getTopicName());
        Number id = simpleJdbcInsertNotification.executeAndReturnKey(params);
        logger.info("Generated a new subscription with id:"+id.longValue());
        notification.setId(id.longValue());
        logger.info("END UserDataRepository addNotification(notification)");
        return notification;
    }

    @Override
    public Topic getTopicDataForLabel(String label) {
        logger.info("START UserDataRepository getTopicDataForLabel()");
        try{
            String query = "select * from topic_details where label = ?";
            Topic topic = jdbcTemplate.queryForObject(query, new TopicMapper(), label);
            return topic;
        } catch(Exception e){
            logger.error("Exception e :"+e.getMessage());
        }
        logger.info("END UserDataRepository getTopicDataForLabel()");
        return null;
    }

    @Override
    public Notification checkPendingNotification() {
        logger.info("START UserDataRepository checkPendingNotification()");
        String query = "SELECT * from notifications";
        List<Notification> notifications = jdbcTemplate.query(query, new NotificationMapper());
        for(Notification notification : notifications) {
            if(notification.getStatus().equals("PENDING")){
                return notification;
            }
        }
        logger.info("END UserDataRepository checkPendingNotification()");
        return null;
    }

    @Override
    public boolean updateNotificationStatus(long notification_id) {
        logger.info("START UserDataRepository updateNotificationStatus()");
        String query = "UPDATE notifications SET status = 'COMPLETED' where notification_id = ?";
        int num = jdbcTemplate.update(query, notification_id);
        logger.info("END UserDataRepository updateNotificationStatus()");
        return num > 0;
    }

    @Override
    public User getUserById(long userId) {
        logger.info("START UserDataRepository getUserById()");
        String query = "select * from user_table where user_id = ?";
        User user = jdbcTemplate.queryForObject(query, new UserMapper(), userId);
        logger.info("END UserDataRepository getUserById()");
        return user;
    }

    @Override
    public List<Subscription> getAllSubscribers() {
        logger.info("START UserDataRepository getAllSubscribers()");
        String query = "select * from subscriber_table";
        List<Subscription> subs = jdbcTemplate.query(query, new SubscriptionMapper());
        logger.info("END UserDataRepository getAllSubscribers()");
        return subs;
    }

    @Override
    public User getUserByName(String name) {
        logger.info("START UserDataRepository getUserByName()");
        String query = "select * from user_table where user_name = ?";
        User user = jdbcTemplate.queryForObject(query, new UserMapper(), name);
        logger.info("END UserDataRepository getUserByName()");
        return user;
    }

    @Override
    public boolean updateSubscription(long userId, String newTopic) {
        logger.info("START UserDataRepository updateSubscription()");
        String query = "UPDATE subscriber_table SET topic_name = ? WHERE user_id = ?";
        int updated = jdbcTemplate.update(query, newTopic, userId);
        logger.info("END UserDataRepository updateSubscription()");
        return updated > 0;
    }

    @Override
    public boolean updateTopicDetails(String topic, Map<String, String> topicData) {
        logger.info("START UserDataRepository updateTopicDetails()");
        String updateCurrencyQuery = "UPDATE topic_details SET "+topic+" = ? WHERE label = \'currency\'";
        int updatedCurrency = jdbcTemplate.update(updateCurrencyQuery, topicData.get("currency"));
        String updateVaccinationsQuery = "UPDATE topic_details SET "+topic+" = ? WHERE label = \'vaccinations\'";
        int updatedVaccinations = jdbcTemplate.update(updateVaccinationsQuery, topicData.get("vaccinations"));
        String updateAdviseQuery = "UPDATE topic_details SET "+topic+" = ? WHERE label = \'advise\'";
        int updatedAdvise = jdbcTemplate.update(updateAdviseQuery, topicData.get("advise"));
        logger.info("END UserDataRepository updateTopicDetails()");
        return updatedCurrency > 0 && updatedVaccinations > 0 && updatedAdvise > 0;
    }

    @Override
    public boolean deleteSubscription(long userId) {
        logger.info("START UserDataRepository deleteSubscription()");
        String query = "DELETE from subscriber_table where user_id = ?";
        int deleted = jdbcTemplate.update(query, userId);
        logger.info("END UserDataRepository deleteSubscription()");
        return deleted > 0;
    }

    @Override
    public List<User> getAllUsers() {
        logger.info("START UserDataRepository getAllUsers()");
        String query = "select * from user_table";
        List<User> users = jdbcTemplate.query(query, new UserMapper());
        logger.info("END UserDataRepository getAllUsers()");
        return users;
    }

    @Override
    public List<Subscription> getAllSubscriptionsByTopic(String topicName) {
        logger.info("START UserDataRepository getAllSubscriptions()");
        String query = "select * from subscriber_table where topic_name = ?";
        List<Subscription> subscriptions = jdbcTemplate.query(query, new SubscriptionMapper(), topicName);
        logger.info("END UserDataRepository getAllSubscriptions()");
        return subscriptions;
    }

    public class UserMapper implements RowMapper<User> {
        public User mapRow(ResultSet rs, int rowNum) throws SQLException{
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setName(rs.getString("user_name"));
            return user;
        }
    }

    public class SubscriptionMapper implements RowMapper<Subscription> {
        public Subscription mapRow(ResultSet rs, int rowNum) throws SQLException{
            Subscription subscription = new Subscription();
            subscription.setId(rs.getLong("subscriber_id"));
            subscription.setUserId(rs.getLong("user_id"));
            subscription.setTopicName(rs.getString("topic_name"));
            return subscription;
        }
    }

    public class TopicMapper implements RowMapper<Topic> {
        public Topic mapRow(ResultSet rs, int rowNum) throws SQLException{
            Topic topic = new Topic();
            topic.setId(rs.getLong("label_id"));
            topic.setLabel(rs.getString("label"));
            topic.setIndia(rs.getString("india"));
            topic.setEgypt(rs.getString("egypt"));
            topic.setSingapore(rs.getString("singapore"));
            return topic;
        }
    }

    public class NotificationMapper implements RowMapper<Notification> {
        public Notification mapRow(ResultSet rs, int rowNum) throws SQLException{
            Notification notification = new Notification();
            notification.setId(rs.getLong("notification_id"));
            notification.setTopicName(rs.getString("topic_name"));
            notification.setStatus(rs.getString("status"));
            return notification;
        }
    }
}
