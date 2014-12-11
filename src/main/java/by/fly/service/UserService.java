package by.fly.service;

import by.fly.model.QUser;
import by.fly.model.User;
import by.fly.repository.UserRepository;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static by.fly.util.Utils.containsIgnoreCasePattern;

@Service
@Transactional
public class UserService {

    public static final String MASTER_CODE_PREFIX = "*M";
    public static final String MASTER  = "master";

    private static final String USER = QUser.user.toString();
    private static final String USER_NAME = QUser.user.name.getMetadata().getName();

    @Autowired
    private UserRepository repository;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private MongoOperations mongoOperations;

    public String generateMasterBarcode() {
        return MASTER_CODE_PREFIX + sequenceService.getNextSequence(MASTER);
    }

    public User findMasterByBarcode(String barcode) {
        return repository.findByBarcode(barcode);
    }

    public void save(User user) {
        repository.save(user);
    }

    public void delete(User user) {
        repository.delete(user);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public List<String> findUserNames(String filter) {
        DBObject query = BasicDBObjectBuilder.start(USER_NAME, containsIgnoreCasePattern(filter)).get();
        return mongoOperations.execute(callback -> callback.getCollection(USER).distinct(USER_NAME, query));
    }

}
