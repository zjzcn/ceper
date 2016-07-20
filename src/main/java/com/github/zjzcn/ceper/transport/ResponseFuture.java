package com.github.zjzcn.ceper.transport;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseFuture extends Response implements Future {
	
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(ResponseFuture.class);
	
	private volatile FutureState state = FutureState.DOING;

	private Object lock = new Object();

	private Object result = null;
	private Exception exception = null;

	private long createTime = System.currentTimeMillis();

	private Request request;
	private List<FutureListener> listeners;

	public ResponseFuture(Request requestObj, int timeout) {
		this.request = requestObj;
		super.setTimeout(timeout);
	}

	public void onSuccess(Response response) {
		this.result = response.getData();
		super.setProcessTime(response.getProcessTime());
		done();
	}

	public void onFailure(Response response) {
		this.exception = response.getException();
		done();
	}

	public long getRequestId() {
		return this.request.getRequestId();
	}
	
	@Override
	public Object get() {
		synchronized (lock) {
			if (!isDoing()) {
				return getResultOrThrowException();
			}

			if (super.getTimeout() <= 0) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					cancel(new RuntimeException("ResponseFuture interrupted : requestId=" 
							+ request.getRequestId() + ", costTimeMs="+ (System.currentTimeMillis() - createTime), e));
				}

				// don't need to notifylisteners, because onSuccess or
				// onFailure or cancel method already call notifylisteners
				return getResultOrThrowException();
			} else {
				long waitTime = super.getTimeout() - (System.currentTimeMillis() - createTime);

				if (waitTime > 0) {
					for (;;) {
						try {
							lock.wait(waitTime);
						} catch (InterruptedException e) {
							// timeout interruted
						}

						if (!isDoing()) {
							break;
						}
						
						waitTime = super.getTimeout() - (System.currentTimeMillis() - createTime);
						if (waitTime <= 0) {
							break;
						}
					}
				}

				if (isDoing()) {
					super.setProcessTime(System.currentTimeMillis() - createTime);
					Exception e = new RuntimeException("ResponseFuture request timeout: requestId=" + request.getRequestId() 
								+ ", costTimeMs="+ (System.currentTimeMillis() - createTime));
					cancel(e);
				}
			}
			return getResultOrThrowException();
		}
	}

	@Override
	public Exception getException() {
		return exception;
	}

	@Override
	public boolean cancel() {
		Exception e = new RuntimeException("ResponseFuture canceled: requestId=" + request.getRequestId()
			+ ", costTimeMs="+ (System.currentTimeMillis() - createTime));
		return cancel(e);
	}
	
	private boolean cancel(Exception e) {
		synchronized (lock) {
			if (!isDoing()) {
				return false;
			}

			state = FutureState.CANCELLED;
			exception = e;
			lock.notifyAll();
		}

		notifyListeners();
		return true;
	}

	@Override
	public boolean isCancelled() {
		return state.isCancelledState();
	}

	@Override
	public boolean isDone() {
		return state.isDoneState();
	}

	@Override
	public boolean isSuccess() {
		return isDone() && (exception == null);
	}

	@Override
	public void addListener(FutureListener listener) {
		if (listener == null) {
			throw new NullPointerException("FutureListener is null");
		}

		boolean notifyNow = false;
		synchronized (lock) {
			if (!isDoing()) {
				// is success, failure, timeout or cancel, don't add into
				// listeners, just notify
				notifyNow = true;
			} else {
				if (listeners == null) {
					listeners = new ArrayList<FutureListener>(1);
				}

				listeners.add(listener);
			}
		}

		if (notifyNow) {
			notifyListener(listener);
		}
	}

	public long getCreateTime() {
		return createTime;
	}

	public Object getRequestObj() {
		return request;
	}

	public FutureState getState() {
		return state;
	}

	private void notifyListeners() {
		if (listeners != null) {
			for (FutureListener listener : listeners) {
				notifyListener(listener);
			}
		}
	}

	private void notifyListener(FutureListener listener) {
		try {
			listener.onComplete(this);
		} catch (Throwable t) {
			logger.error("NettyResponseFuture notifyListener Error: " + listener.getClass().getSimpleName(), t);
		}
	}

	private boolean isDoing() {
		return state.isDoingState();
	}

	private boolean done() {
		synchronized (lock) {
			if (!isDoing()) {
				return false;
			}

			state = FutureState.DONE;
			lock.notifyAll();
		}

		notifyListeners();
		return true;
	}

	private Object getResultOrThrowException() {
		if (exception != null) {
			throw (exception instanceof RuntimeException) ? (RuntimeException) exception : new RuntimeException( exception);
		}

		return result;
	}

}