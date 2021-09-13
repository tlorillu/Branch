package io.branch.referral;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * <p>The Branch SDK can queue up requests whilst it is waiting for initialization of a session to
 * complete. This allows you to start sending requests to the Branch API as soon as your app is
 * opened.</p>
 */
class ServerRequestQueue {
    private static final String PREF_KEY = "BNCServerRequestQueue";
    private static final int MAX_ITEMS = 25;
    private static ServerRequestQueue SharedInstance;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private final List<ServerRequest> queue;
    //Object for synchronising operations on server request queue
    private static final Object reqQueueLockObject = new Object();
    
    /**
     * <p>Singleton method to return the pre-initialised, or newly initialise and return, a singleton
     * object of the type {@link ServerRequestQueue}.</p>
     *
     * @param c A {@link Context} from which this call was made.
     * @return An initialised {@link ServerRequestQueue} object, either fetched from a
     * pre-initialised instance within the singleton class, or a newly instantiated
     * object where one was not already requested during the current app lifecycle.
     */
    public static ServerRequestQueue getInstance(Context c) {
        if (SharedInstance == null) {
            synchronized (ServerRequestQueue.class) {
                if (SharedInstance == null) {
                    SharedInstance = new ServerRequestQueue(c);
                }
            }
        }
        return SharedInstance;
    }

    // Package Private
    static void shutDown() {
        synchronized (reqQueueLockObject) {
            SharedInstance = null;
        }
    }
    
    /**
     * <p>The main constructor of the ServerRequestQueue class is private because the class uses the
     * Singleton pattern.</p>
     *
     * @param c A {@link Context} from which this call was made.
     */
    @SuppressLint("CommitPrefEdits")
    private ServerRequestQueue(Context c) {
        sharedPref = c.getSharedPreferences("BNC_Server_Request_Queue", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        queue = retrieve(c);
    }
    
    private void persist() {
        try {
            JSONArray jsonArr = new JSONArray();
            synchronized (reqQueueLockObject) {
                for (ServerRequest aQueue : queue) {
                    if (aQueue.isPersistable()) {
                        JSONObject json = aQueue.toJSON();
                        if (json != null) {
                            jsonArr.put(json);
                        }
                    }
                }
            }

            editor.putString(PREF_KEY, jsonArr.toString()).apply();
        } catch (Exception ex) {
            String msg = ex.getMessage();
            PrefHelper.Debug("Failed to persist queue" + (msg == null ? "" : msg));
        }
    }
    
    private List<ServerRequest> retrieve(Context context) {
        String jsonStr = sharedPref.getString(PREF_KEY, null);
        List<ServerRequest> result = Collections.synchronizedList(new LinkedList<ServerRequest>());
        synchronized (reqQueueLockObject) {
            if (jsonStr != null) {
                try {
                    JSONArray jsonArr = new JSONArray(jsonStr);
                    for (int i = 0, size = Math.min(jsonArr.length(), MAX_ITEMS); i < size; i++) {
                        JSONObject json = jsonArr.getJSONObject(i);
                        ServerRequest req = ServerRequest.fromJSON(json, context);
                        if (req != null) {
                            result.add(req);
                        }
                    }
                } catch (JSONException ignored) {
                }
            }
        }
        return result;
    }
    
    /**
     * <p>Gets the number of {@link ServerRequest} objects currently queued up for submission to
     * the Branch API.</p>
     *
     * @return An {@link Integer} value indicating the current size of the {@link List} object
     * that forms the logical queue for the class.
     */
    public int getSize() {
        synchronized (reqQueueLockObject) {
            return queue.size();
        }
    }
    
    /**
     * <p>Adds a {@link ServerRequest} object to the queue.</p>
     *
     * @param request The {@link ServerRequest} object to add to the queue.
     */
    void enqueue(ServerRequest request) {
        synchronized (reqQueueLockObject) {
            if (request != null) {
                queue.add(request);
                if (getSize() >= MAX_ITEMS) {
                    queue.remove(1);
                }
                persist();
            }
        }
    }
    
    /**
     * <p>Gets the queued {@link ServerRequest} object at position with index 0 within the queue
     * without removing it.</p>
     *
     * @return The {@link ServerRequest} object at position with index 0 within the queue.
     */
    ServerRequest peek() {
        ServerRequest req = null;
        synchronized (reqQueueLockObject) {
            try {
                req = queue.get(0);
            } catch (IndexOutOfBoundsException | NoSuchElementException ignored) {
            }
        }
        return req;
    }
    
    /**
     * <p>Gets the queued {@link ServerRequest} object at position with index specified in the supplied
     * parameter, within the queue. Like {@link #peek()}, the item is not removed from the queue.</p>
     *
     * @param index An {@link Integer} that specifies the position within the queue from which to
     *              pull the {@link ServerRequest} object.
     * @return The {@link ServerRequest} object at the specified index. Returns null if no
     * request exists at that position, or if the index supplied is not valid, for
     * instance if {@link #getSize()} is 6 and index 6 is called.
     */
    ServerRequest peekAt(int index) {
        ServerRequest req = null;
        synchronized (reqQueueLockObject) {
            try {
                req = queue.get(index);
            } catch (IndexOutOfBoundsException | NoSuchElementException ignored) {
            }
        }
        return req;
    }
    
    /**
     * <p>As the method name implies, inserts a {@link ServerRequest} into the queue at the index
     * position specified.</p>
     *
     * @param request The {@link ServerRequest} to insert into the queue.
     * @param index   An {@link Integer} value specifying the index at which to insert the
     *                supplied {@link ServerRequest} object. Fails silently if the index
     *                supplied is invalid.
     */
    void insert(ServerRequest request, int index) {
        synchronized (reqQueueLockObject) {
            try {
                if (queue.size() < index) {
                    index = queue.size();
                }
                queue.add(index, request);
                persist();
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
    }
    
    /**
     * <p>As the method name implies, removes the {@link ServerRequest} object, at the position
     * indicated by the {@link Integer} parameter supplied.</p>
     *
     * @param index An {@link Integer} value specifying the index at which to remove the
     *              {@link ServerRequest} object. Fails silently if the index
     *              supplied is invalid.
     * @return The {@link ServerRequest} object being removed.
     */
    @SuppressWarnings("unused")
    public ServerRequest removeAt(int index) {
        ServerRequest req = null;
        synchronized (reqQueueLockObject) {
            try {
                req = queue.remove(index);
                persist();
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
        return req;
    }
    
    /**
     * <p>As the method name implies, removes {@link ServerRequest} supplied in the parameter if it
     * is present in the queue.</p>
     *
     * @param request The {@link ServerRequest} object to be removed from the queue.
     * @return A {@link Boolean} whose value is true if the object is removed.
     */
    public boolean remove(ServerRequest request) {
        boolean isRemoved = false;
        synchronized (reqQueueLockObject) {
            try {
                isRemoved = queue.remove(request);
                persist();
            } catch (UnsupportedOperationException ignored) {
            }
        }
        return isRemoved;
    }
    
    /**
     * <p> Clears all pending requests in the queue </p>
     */
    void clear() {
        synchronized (reqQueueLockObject) {
            try {
                queue.clear();
                persist();
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }
    
    /**
     * <p>Determines whether the queue contains an install/register request.</p>
     *
     * @return A {@link Boolean} value indicating whether or not the queue contains an
     * install/register request. <i>True</i> if the queue contains a close request,
     * <i>False</i> if not.
     */
    ServerRequestInitSession getSelfInitRequest() {
        synchronized (reqQueueLockObject) {
            for (ServerRequest req : queue) {
                if (req instanceof ServerRequestInitSession) {
                    ServerRequestInitSession r = (ServerRequestInitSession) req;
                    if (r.initiatedByClient) {
                        return r;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Set Process wait lock to false for any open / install request in the queue
     */
    void unlockProcessWait(ServerRequest.PROCESS_WAIT_LOCK lock) {
        synchronized (reqQueueLockObject) {
            for (ServerRequest req : queue) {
                if (req != null) {
                    req.removeProcessWaitLock(lock);
                }
            }
        }
    }
    
    /**
     * Sets the strong match wait for any init session request in the queue
     */
    void setStrongMatchWaitLock() {
        synchronized (reqQueueLockObject) {
            for (ServerRequest req : queue) {
                if (req != null) {
                    if (req instanceof ServerRequestInitSession) {
                        req.addProcessWaitLock(ServerRequest.PROCESS_WAIT_LOCK.STRONG_MATCH_PENDING_WAIT_LOCK);
                    }
                }
            }
        }
    }
}
