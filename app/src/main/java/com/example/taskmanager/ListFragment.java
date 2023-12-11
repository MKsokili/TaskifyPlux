package com.example.taskmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanager.CustomerClass.UserClass;
import com.example.taskmanager.TaskList.MyTaskListAdapter;
import com.example.taskmanager.TaskList.TaskClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ListFragment extends Fragment {

    Context thisFragmentContext, context;

    // Set user variables
    String userId, userName, userRole;

    boolean selectable = false;      // indicate whether the Patient drop-down menu is selectable or not

    SearchView searchView;
    AutoCompleteTextView tvPatientFilter, tvCategoryFilter;
    private RecyclerView rvList;
    private RecyclerView.LayoutManager taskListLayoutManager;
    private RecyclerView.Adapter taskListAdapter;
    private ArrayList<TaskClass> taskList;
    private ArrayList<TaskClass> filteredTaskList;

//--------------------------------------------------------------------------------------------------------------------
    // connection to Firebase Realtime database
    FirebaseDatabase realtime_db = FirebaseDatabase.getInstance();

    // connection to Firebase Firestore database
    private FirebaseFirestore firestore_db = FirebaseFirestore.getInstance();
    private CollectionReference taskCollection = firestore_db.collection("Tasks");  // tasks collection
    private CollectionReference userCollection = firestore_db.collection("Users");  // users collection

//--------------------------------------------------------------------------------------------------------------------
// Setup Patient drop-down menu
    ArrayList<UserClass> patientList = new ArrayList<>();  // to store patient list get from Firestore db
    ArrayList<String> patientNameList = new ArrayList<>();  // to store patient name
    ArrayAdapter<String> patientAdapter;
    String selectedPatient = "All";

    String selectedPatientId;

    Map<String, String> patientMap = new HashMap<>();

//--------------------------------------------------------------------------------------------------------------------
    // Setup Category drop-down menu
    String[] categories = {"All", "Planning", "Development", "Testing", "Others"};
    AutoCompleteTextView tvCategory;
    ArrayAdapter<String> categoryAdapter;
    String selectedCategory = "All";
//--------------------------------------------------------------------------------------------------------------------
    // this part is to receive message from the foreground service
    // once receive foreground service call, LoadDataFromDB() will be triggered to update data
    private BroadcastReceiver dataUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("LOAD_DATA_FROM_DB")) {
                // Call your function when the broadcast is received
                LoadDataFromDB();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("LOAD_DATA_FROM_DB");
        requireContext().registerReceiver(dataUpdateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(dataUpdateReceiver);
    }

//--------------------------------------------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        thisFragmentContext = requireContext();
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        context = getContext();
        // To restore the Patient Filter
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
            selectedPatient = sharedPreferences.getString("selectedPatient", ""); // default value as empty string
            selectedPatientId = sharedPreferences.getString("selectedPatientId", "");
        }

        rvList = view.findViewById(R.id.rvList);
        tvPatientFilter = view.findViewById(R.id.tvPatientFilter);
        tvCategoryFilter = view.findViewById(R.id.tvCategoryFilter);

        searchView = view.findViewById(R.id.searchView);

        taskList = new ArrayList<>();
        filteredTaskList = new ArrayList<>();

//--------------------------------------------------------------------------------------------------------------------
        // Load user name and uer id
        // Since we are using Fragment which does not have its own Intent.
        // We need to access the intent from the host activity that contains the Fragment
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            userId = bundle.getString("userId");
            userName = bundle.getString("userName");
            userRole = bundle.getString("userRole");
            if (userRole.equals("Doctor"))
                selectable = true;
        }
//--------------------------------------------------------------------------------------------------------------------
        // get Patient list from Firestore
        // and set the Patient drop-down menu
        if (!selectable) {
            // if the user is a patient, show the user's name disable the drop-down menu
            tvPatientFilter.setText(userName);
            tvPatientFilter.setFocusable(false);
            tvPatientFilter.setFocusableInTouchMode(false);
            tvPatientFilter.setInputType(InputType.TYPE_NULL);
            tvPatientFilter.setOnClickListener(null);
            tvPatientFilter.setOnTouchListener(null);
        }
        else {
            // if the user is a doctor, enable the patient drop-down menu
            patientList.clear();
            patientNameList.clear();
            userCollection
                    .whereEqualTo("userRole", "Patient")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                //patientNameList.add("All");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    UserClass user = document.toObject(UserClass.class);
                                    patientList.add(user);
                                    patientNameList.add(user.getUserName());

                                    // save patient name and the associated patientId to hash map
                                    for (int i = 0; i <= patientList.size() - 1; i++) {
                                        patientMap.put(patientList.get(i).getUserName(), patientList.get(i).getUserId());
                                    }

                                    // Add Patient to the Patient drop-down menu
                                    // Use the new dropdown_item_layout.xml for the adapter
                                    patientAdapter = new ArrayAdapter<String>(thisFragmentContext, R.layout.dropdown_item_layout, patientNameList);

                                    // Specify the layout resource for dropdown items
                                    patientAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                                    tvPatientFilter.setAdapter(patientAdapter);

                                    if (selectedPatient != null) {
                                        if (!selectedPatient.equals(""))    // if empty
                                            tvPatientFilter.setText(selectedPatient, false);
                                    }

                                    tvPatientFilter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            selectedPatient = parent.getItemAtPosition(position).toString();
                                            selectedPatientId = patientMap.get(tvPatientFilter.getText().toString());
                                            LoadDataFromDB();
                                            //Toast.makeText(thisFragmentContext, selectedPatient + " : " + selectedCategory, Toast.LENGTH_LONG).show();
                                            filterTask(searchView.getQuery().toString());
                                            setTaskList(filteredTaskList);  // Load the filtered data to RecyclerView
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(thisFragmentContext, "Error getting user data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
//--------------------------------------------------------------------------------------------------------------------
        // Add categories to the Category drop-down menu
        // Use the new dropdown_item_layout.xml for the adapter
        categoryAdapter = new ArrayAdapter<String>(thisFragmentContext, R.layout.dropdown_item_layout, categories);

        // Specify the layout resource for dropdown items
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        tvCategoryFilter.setAdapter(categoryAdapter);
        //tvCategoryFilter.setText(categoryAdapter.getItem(0), false);
        tvCategoryFilter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory =  parent.getItemAtPosition(position).toString();
                //Toast.makeText(thisFragmentContext, selectedCategory, Toast.LENGTH_SHORT).show();
                filterTask(searchView.getQuery().toString());
                setTaskList(filteredTaskList);  // Load the filtered data to RecyclerView
            }
        });

//--------------------------------------------------------------------------------------------------------------------
        LoadDataFromDB();

        // Handle search function
        // Use SearchView to search data
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //String userInput = etSearch.getText().toString();
                //searchTask(newText);
                filterTask(newText);
                setTaskList(filteredTaskList);  // Load the filtered data to RecyclerView
                //Toast.makeText(thisFragmentContext, userInput, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        return view;
    }
//--------------------------------------------------------------------------------------------------------------------

    private void LoadDataFromDB() {
        // Load all data for the logged-in user

        String patientId = userId;

        // Clear the lists first
        taskList.clear();
        Query query;
        if (userRole.equals("Doctor")) {    //doctor can see all the data
            // need to create index in Firestore first, click the link in the error msg
            query = taskCollection
                    .whereEqualTo("status", "Pending")
                    .whereEqualTo("patientId", selectedPatientId)
                    .orderBy("year", Query.Direction.ASCENDING)
                    .orderBy("month", Query.Direction.ASCENDING)
                    .orderBy("day", Query.Direction.ASCENDING)
                    .orderBy("hour", Query.Direction.ASCENDING)
                    .orderBy("minute", Query.Direction.ASCENDING);
            //.orderBy("day");
            //.whereEqualTo("category", category);
        } else {    // patient only view their own data
            // need to create index in Firestore first, click the link in the error msg
            query = taskCollection
                    .whereEqualTo("patientId", patientId)
                    .whereEqualTo("status", "Pending")
                    .orderBy("year", Query.Direction.ASCENDING)
                    .orderBy("month", Query.Direction.ASCENDING)
                    .orderBy("day", Query.Direction.ASCENDING)
                    .orderBy("hour", Query.Direction.ASCENDING)
                    .orderBy("minute", Query.Direction.ASCENDING);
        }

        // Execute the query to get the matching documents
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        TaskClass eachTask = document.toObject(TaskClass.class);
                        eachTask.setId(document.getId());       // To get document id for further update or delete
                        taskList.add(eachTask);     // Load the data to taskList
                        setTaskList(taskList);  // Load the filtered data to RecyclerView
                    }

                } else {
                    // Display the error
                    Toast.makeText(thisFragmentContext, task.getException().toString(), Toast.LENGTH_LONG).show();
                    Log.d("Firestore error", task.getException().toString());
                }
            }
        });

    }

//--------------------------------------------------------------------------------------------------------------------
    // Feed the task list to RecyclerView
    //set the content of Task List
    private void setTaskList(ArrayList<TaskClass> taskList) {

        taskListAdapter = new MyTaskListAdapter(taskList, userRole, selectedPatientId, thisFragmentContext);
        taskListLayoutManager = new LinearLayoutManager(thisFragmentContext);
        rvList.setLayoutManager(taskListLayoutManager);
        rvList.setAdapter(taskListAdapter);
        taskListAdapter.notifyDataSetChanged();
    }

    private void filterTask(String searchString) {

        if (filteredTaskList != null)
            filteredTaskList.clear();

        searchString = searchString.toLowerCase();

            if (selectedCategory.equals("All")) {   // Category = All
                for (TaskClass eachTask : taskList) {
                    String taskTitle = eachTask.getTaskTitle().toLowerCase();
                    if (searchString.equals("")) {  // if search string is empty
                        filteredTaskList.add(eachTask);
                    } else {
                        if (taskTitle.contains((searchString))) {   // if search string is not empty
                            filteredTaskList.add(eachTask);
                        }
                    }
                }
            } else {                                // Category = not All
                for (TaskClass eachTask : taskList) {
                    String taskTitle = eachTask.getTaskTitle().toLowerCase();
                    if (eachTask.getCategory().equals(selectedCategory)) {
                        if (searchString.equals("")) {      // if search string is empty
                            filteredTaskList.add(eachTask);
                        } else {
                            if (taskTitle.contains((searchString))) {  // if search string is not empty
                                filteredTaskList.add(eachTask);
                            }
                        }
                    }
                }
            }
    }

    private void searchTask(String searchString) {
        if (filteredTaskList != null)
            filteredTaskList.clear();

        for (TaskClass eachTask : taskList) {
            if (eachTask.getTaskTitle().contains(searchString)) {
                filteredTaskList.add(eachTask);
            }
        }
    }
}