package utils.scorer;

public class ScorerService {

	private volatile IChatScorer scorer; 
	public IChatScorer getScorer() {
		if (scorer == null) {
			synchronized (this) {
				if (scorer == null) {
					scorer = new ChatScorer();
				}
			}
		}
		return scorer;
	}
}
