package utils.scorer;

public interface IChatScorer {
	int score(String message) throws ServiceUnavailableException;
}
