package me.dillbox.mailmanmod;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.OkHttpClient;
import okhttp3.FormBody;

public class MailmanHTTPHelper {
    private static final int VOLLEY_TIMEOUT = 10000;
    private static final int TIMEOUT_SECONDS = 10;

    private static final int MAILMAN_DEFER = 0;
    private static final int MAILMAN_ACCEPT = 1;
    private static final int MAILMAN_DISCARD = 3;

    static class MailmanHTTPException extends Exception {
        private Throwable mCause;

        public Throwable getCause() {
            return mCause;
        }

        MailmanHTTPException(String msg) {
            super(msg);
        }
        MailmanHTTPException(String msg, Throwable cause) {
            super(msg != null ? msg : cause.getMessage());
            mCause = cause;
        }
    }

    public static void modifyDatabase(String url, String password, List<Integer> accept, List<Integer> defer, boolean discardDeferred, Context context) throws MailmanHTTPException {
        if (url.length() == 0) {
            throw new MailmanHTTPException("URL is not set");
        }
        if (password.length() == 0) {
            throw new MailmanHTTPException("Password is not set");
        }

        RequestQueue rq = Volley.newRequestQueue(context);

        RequestFuture<String> future = RequestFuture.newFuture();

        StringRequest sr = new StringRequest(Request.Method.POST, url, future, future) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("adminpw", password);
                for (Integer msgId : accept) {
                    params.put(msgId + "", MAILMAN_ACCEPT + "");
                }
                for (Integer msgId : defer) {
                    params.put(msgId + "", (discardDeferred ? MAILMAN_DISCARD : MAILMAN_DEFER) + "");
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Connection", "close");
                headers.put("Accept-Encoding", "identity");
                return headers;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                VOLLEY_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rq.add(sr);

        try {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof AuthFailureError) {
                throw new MailmanHTTPException("Login failed!", e.getCause());
            } else if (e.getCause() instanceof TimeoutError) {
                throw new MailmanHTTPException("Refresh timed out.", e.getCause());
            } else {
                throw new MailmanHTTPException(null, e.getCause());
            }
        } catch (TimeoutException e) {
            throw new MailmanHTTPException("Refresh timed out.", e);
        }
    }

    public static List<Integer> fetchMailQueueIds(String url, String password, Context context) throws MailmanHTTPException {
        if (url.length() == 0) {
            throw new MailmanHTTPException("URL is not set");
        }
        if (password.length() == 0) {
            throw new MailmanHTTPException("Password is not set");
        }

        RequestQueue rq = Volley.newRequestQueue(context);

        RequestFuture<String> future = RequestFuture.newFuture();

        StringRequest sr = new StringRequest(Request.Method.POST, url, future, future) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("adminpw", password);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Connection", "close");
                headers.put("Accept-Encoding", "identity");
                return headers;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                VOLLEY_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rq.add(sr);

        try {
            return MailmanParser.mailQueueFromHtml(future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof AuthFailureError) {
                throw new MailmanHTTPException("Login failed!", e.getCause());
            } else if (e.getCause() instanceof TimeoutError) {
                throw new MailmanHTTPException("Refresh timed out.", e.getCause());
            } else {
                throw new MailmanHTTPException(null, e.getCause());
            }
        } catch (TimeoutException e) {
            throw new MailmanHTTPException("Refresh timed out.", e);
        }
    }

    public static HeldMail fetchMsgId(String list, int msgId, String url, String password, Context context) throws MailmanHTTPException {
        if (url.length() == 0) {
            throw new MailmanHTTPException("URL is not set");
        }
        if (password.length() == 0) {
            throw new MailmanHTTPException("Password is not set");
        }

        RequestQueue rq = Volley.newRequestQueue(context);

        RequestFuture<String> future = RequestFuture.newFuture();

        StringRequest sr = new StringRequest(Request.Method.POST, url+"?msgid="+msgId, future, future) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("adminpw", password);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Connection", "close");
                headers.put("Accept-Encoding", "identity");
                return headers;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                VOLLEY_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rq.add(sr);

        try {
            return MailmanParser.heldMailFromHtml(future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS), msgId, list);
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof AuthFailureError) {
                throw new MailmanHTTPException("Login failed!", e.getCause());
            } else if (e.getCause() instanceof TimeoutError) {
                throw new MailmanHTTPException("Refresh timed out.", e.getCause());
            } else {
                throw new MailmanHTTPException(null, e.getCause());
            }
        } catch (TimeoutException e) {
            throw new MailmanHTTPException("Refresh timed out.", e);
        } catch (MailmanParser.MailmanParserException e) {
            throw new MailmanHTTPException("Could not parse HTML.", e);
        }
    }
}
