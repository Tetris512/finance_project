package finance_backend.dao;

import finance_backend.pojo.entity.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackDao extends JpaRepository<FeedbackEntity, Long> {
    FeedbackEntity findByUserAndConversationIdAndMessageId(String user, String conversationId, String messageId);

}