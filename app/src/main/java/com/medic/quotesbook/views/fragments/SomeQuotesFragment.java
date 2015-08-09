package com.medic.quotesbook.views.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.medic.quotesbook.R;

import com.medic.quotesbook.tasks.GetSomeQuotesTask;
import com.medic.quotesbook.utils.ChangeActivityRequestListener;
import com.medic.quotesbook.views.adapters.QuotesAdapter;
import com.medic.quotesbook.views.fragments.dummy.DummyContent;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class SomeQuotesFragment extends Fragment{

    private final String TAG = "SomeQuotesFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private RecyclerView recyclerView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static SomeQuotesFragment newInstance(String param1, String param2) {
        SomeQuotesFragment fragment = new SomeQuotesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SomeQuotesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // TODO: Change Adapter to display your content
        mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_somequotes, container, false);

        View loaderLayout = view.findViewById(R.id.loader_layout);
        View quotesView = view.findViewById(R.id.quotes_list);

        // Set the adapter
        recyclerView = (RecyclerView) view.findViewById(R.id.quotes_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        QuotesAdapter adapter = new QuotesAdapter(null, (ChangeActivityRequestListener) getActivity());

        GetSomeQuotesTask task = new GetSomeQuotesTask(adapter, loaderLayout, quotesView);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener(){

            GetSomeQuotesTask nextTask = null;

            @Override
            public void onScrolled( RecyclerView view, int dx, int dy){

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                int remainingItems = totalItemCount - pastVisiblesItems;

                if (remainingItems <= 6 && (nextTask == null || !nextTask.isLoading()) ){
                    nextTask = new GetSomeQuotesTask((QuotesAdapter) view.getAdapter());
                    nextTask.execute();
                }

                //Log.d(TAG, "Visibles: " + Integer.toString(visibleItemCount) + ", totals: " + Integer.toString(totalItemCount) + ", past: " + Integer.toString(pastVisiblesItems) + ", remaining: " + Integer.toString(remainingItems));
            }
        });

        task.execute();

        // Set OnItemClickListener so we can be notified on item clicks
        //recyclerView.setOnItemClickListener(this);

        return view;
    }

    /*
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }*/

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    /*public void setEmptyText(CharSequence emptyText) {
        View emptyView = recyclerView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
