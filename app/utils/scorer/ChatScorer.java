package utils.scorer;

class ChatScorer implements IChatScorer {

	@Override
	public int score(String message) throws ServiceUnavailableException {
		if (Math.random() >= 0.96) {
			try {
				Thread.sleep(9500);
			} catch (InterruptedException e) {
			}
			throw new ServiceUnavailableException();
		} else {
			final long timeout = Math.round(Math.random() * 450);
			sleepUntil(System.currentTimeMillis() + timeout);
			return (int) Math.round(message.length() * Math.PI * 42);
		}		
	}
	
	private static void sleepUntil(final long end) {
		long now;
		while ((now = System.currentTimeMillis()) < end) {
			try {
				Thread.sleep(end - now);
			} catch (InterruptedException e) {
			}
		}
	}

}
