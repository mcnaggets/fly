package by.fly.service;

import by.fly.model.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class SequenceService {

    @Autowired
    private MongoOperations mongoOperations;

    public long getNextSequence(String key) {
        Query query = new Query(Criteria.where("_id").is(key));
        if (mongoOperations.findOne(query, Sequence.class) == null) {
            mongoOperations.insert(new Sequence(key, 0));
        }

        Update update = new Update();
        update.inc("seq", 1);

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);

        Sequence sequence =
                mongoOperations.findAndModify(query, update, options, Sequence.class);

        return sequence.getSeq();

    }

}
