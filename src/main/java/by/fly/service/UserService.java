package by.fly.service;

import by.fly.model.User;
import by.fly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    public static final String MASTER_CODE_PREFIX = "*M";
    public static final String MASTER  = "master";

    @Autowired
    private UserRepository repository;

    @Autowired
    private SequenceService sequenceService;

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
}
