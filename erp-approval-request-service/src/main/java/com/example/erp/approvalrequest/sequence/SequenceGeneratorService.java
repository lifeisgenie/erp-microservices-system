package com.example.erp.approvalrequest.sequence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class SequenceGeneratorService {

    private final MongoOperations mongoOperations;

    @Autowired
    public SequenceGeneratorService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    public int getNextSequence(String seqName) {
        Query query = new Query(Criteria.where("_id").is(seqName));
        Update update = new Update().inc("seq", 1);

        FindAndModifyOptions options = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(true);

        DatabaseSequence sequence = mongoOperations.findAndModify(query, update, options, DatabaseSequence.class);
        if (sequence == null) {
            // 처음 생성될 때
            return 1;
        }
        long value = sequence.getSeq();
        if (value > Integer.MAX_VALUE) {
            throw new IllegalStateException("Sequence overflow for: " + seqName);
        }
        return (int) value;
    }
}