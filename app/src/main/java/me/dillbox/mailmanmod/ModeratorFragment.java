package me.dillbox.mailmanmod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

public class ModeratorFragment extends Fragment {

    private static final String ARG_MAILING_LIST = "mailing_list";

    private RecyclerView recyclerView;
    private MailQueueAdapter mHeldAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private SwipeRefreshLayout mHeldSwipeLayout;

    private MailmanViewModel mMailmanViewModel;

    public static ModeratorFragment newInstance(String list) {
        ModeratorFragment fragment = new ModeratorFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_MAILING_LIST, list);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean absDates = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("show_abs_date", false);
            if (absDates) return;
            mHeldAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        final IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        getContext().registerReceiver(receiver, filter);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = (RecyclerView) root.findViewById(R.id.held_recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        mHeldAdapter = new MailQueueAdapter(getActivity());
        recyclerView.setAdapter(mHeldAdapter);

        mMailmanViewModel = ViewModelProviders.of(this).get(MailmanViewModel.class);
        final String list;
        if (getArguments() != null) {
            list = getArguments().getString(ARG_MAILING_LIST);
        } else {
            list = "";
        }
        Log.i("mqueue", "onCreateView: "+list);
        mMailmanViewModel.getMailQueue(list).observe(this, new Observer<List<HeldMail>>() {
            @Override
            public void onChanged(List<HeldMail> mailQueue) {
                mHeldAdapter.setMailQueue(mailQueue);
            }
        });

        mHeldSwipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.held_swipe_layout);
        SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MailmanRepository repository = MailmanRepository.getInstance(getContext());

                            String url = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(list+"_url", "");
                            String password = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(list+"_password", "");

                            List<Integer> msgId_list = MailmanHTTPHelper.fetchMailQueueIds(url, password, getContext());
                            List<Integer> oldMsgId_list = repository.findOldMsgId(list, msgId_list);
                            for (Integer msgId : oldMsgId_list) {
                                mMailmanViewModel.delete(list, msgId);
                            }
                            for (Integer msgId : msgId_list) {
                                if (!repository.hasMsgId(list, msgId)) {
                                    HeldMail mail = MailmanHTTPHelper.fetchMsgId(list, msgId, url, password, getContext());
                                    mMailmanViewModel.insert(mail);
                                }
                            }
                        } catch (MailmanHTTPHelper.MailmanHTTPException e) {
                            mMailmanViewModel.deleteAll(list);
                            if (e.getCause() != null) {
                                Log.e("blyat", "run: ", e.getCause());
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            });
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mHeldSwipeLayout.setRefreshing(false);
                            }
                        });
                    }
                }).start();
            }
        };
        mHeldSwipeLayout.setOnRefreshListener(listener);
        mHeldSwipeLayout.setColorSchemeResources(
                R.color.rainbow_red,
                R.color.rainbow_orange,
                R.color.rainbow_yellow,
                R.color.rainbow_green,
                R.color.rainbow_blue,
                R.color.rainbow_purple
        );

        class AcceptClickListener implements View.OnClickListener {
            boolean mDiscard;

            AcceptClickListener(boolean discard) {
                mDiscard = discard;
            }

            void apply() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MailmanRepository repository = MailmanRepository.getInstance(getContext());
                            String url = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(list+"_url", "");
                            String password = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(list+"_password", "");
                            List<Integer> accept = mHeldAdapter.findAcceptMsgId(true);
                            List<Integer> defer = mHeldAdapter.findAcceptMsgId(false);
                            /*NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                            for (Integer msgId : accept) {
                                notificationManager.cancel(list.hashCode()+msgId);
                            }
                            for (Integer msgId : defer) {
                                notificationManager.cancel(list.hashCode()+msgId);
                            }*/
                            // TODO cancel notifications
                            MailmanHTTPHelper.modifyDatabase(url, password, accept, defer, mDiscard, getContext());
                        } catch (MailmanHTTPHelper.MailmanHTTPException e) {
                            mMailmanViewModel.deleteAll(list);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            });
                        }

                        mHeldSwipeLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                mHeldSwipeLayout.setRefreshing(true);
                                listener.onRefresh();
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onClick(View view) {
                if (!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("auto_confirm", false)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.confirmation_title)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    apply();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    apply();
                }
            }
        }

        ((Button) root.findViewById(R.id.accept_hold)).setOnClickListener(new AcceptClickListener(false));
        ((Button) root.findViewById(R.id.accept_discard)).setOnClickListener(new AcceptClickListener(true));

        return root;
    }

}