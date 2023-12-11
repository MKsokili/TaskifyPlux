package com.example.taskmanager;

import static com.example.taskmanager.Utility.CalculateDate.dayFromDate;
import static com.example.taskmanager.Utility.CalculateDate.monthFromDate;
import static com.example.taskmanager.Utility.CalculateDate.monthYearFromDate;
import static com.example.taskmanager.Utility.CalculateDate.yearFromDate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanager.Calendar.DateClass;
import com.example.taskmanager.Calendar.MyCalendarAdapter;
import com.example.taskmanager.CustomerClass.UserClass;
import com.example.taskmanager.TaskList.MyTaskListAdapter;
import com.example.taskmanager.TaskList.TaskCategoryClass;
import com.example.taskmanager.TaskList.TaskClass;
import com.example.taskmanager.Utility.CalculateDate;
import com.example.taskmanager.Service.MyNotificationReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class HomeFragment extends Fragment implements MyCalendarAdapter.OnItemClickListener {

    Context thisFragmentContext, context;

    View view;
    public String userName, userId, userEmail, userRole;

    private TextView tvUserName;

    private ImageButton btnAdd;

//------------------------------------------------------------------
    // for calendar
    private TextView tvMonthYear, tvFwd, tvBwd;     // TextView for choosing the month of calendar

    private TextView tvPendingTaskTitle;
    private RecyclerView rvCalendar;
    private RecyclerView.LayoutManager calendarLayoutManager;
    private RecyclerView.Adapter calendarAdapter;
    private ArrayList<String> daysInMonth;


    private LocalDate selectedDate;
    private DateClass dateClass;      //custom date class

    private String clickedDate;     // to store the user clicked date
//------------------------------------------------------------------

    //set Task List
    private RecyclerView rvDashboardTaskList;
    private RecyclerView.LayoutManager taskListLayoutManager;
    private RecyclerView.Adapter taskListAdapter;
    private ArrayList<TaskClass> taskList;
    private ArrayList<TaskClass> originalTaskList;
    ArrayList<TaskCategoryClass> taskInDay;
//    private ArrayList<TaskCategoryClass> taskInDay;
    int[] appointment = new int[32];
    int[] medicine = new int[32];
    int[] workout = new int[32];
    int[] others = new int[32];
    int[] Development= new int[32];
    int[] Testing= new int[32];
    int[] Planning = new int[32];


    //--------------------------------------------------------------------------------------------------------------------
    // Setup Patient drop-down menu
    ArrayList<UserClass> patientList = new ArrayList<>();  // to store patient list get from Firestore db
    ArrayList<String> patientNameList = new ArrayList<>();  // to store patient name
    ArrayAdapter<String> patientAdapter;

    Map<String, String> patientMap = new HashMap<>();

    String selectedPatient;

    String selectedPatientId;

    //String selectedPatientFromSavedPreference,selectedPatientIdFromSavedPreference;
    AutoCompleteTextView tvHomePatientFilter;
    boolean selectable = false;      // indicate whether the Patient drop-down menu is selectable or not


//--------------------------------------------------------------------------------------------------------------------

    // connection to Firebase Realtime database
    //FirebaseDatabase realtime_db = FirebaseDatabase.getInstance();

    // connection to Firebase Firestore database
    private FirebaseFirestore firestore_db = FirebaseFirestore.getInstance();
    private CollectionReference taskCollection = firestore_db.collection("Tasks");  // tasks collection
    private CollectionReference userCollection = firestore_db.collection("Users");  // users collection
//------------------------------------------------------------------
    // for notification
    private static final String CHANNEL_ID = "task4175";
//------------------------------------------------------------------
    // this part is to receive message from the foreground service
    // once receive foreground service call, LoadDataFromDB() will be triggered to update data
    // however, this part cannot be executed if the app was killed
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


//-----------------------------------------------------------------------------------------------------------
    // If the user click a button and navigate to other fragments, when the user return to this fragment,
    // the selected item in the Patient filter was gone.
    // To solve this problem, the item name will be saved before switching to other fragments
    // Th item can be restored in onCreateView()


    public void onPause() {
        super.onPause();
        //saveSelectedItem(getContext(), autoCompleteTextView.getText().toString());
        /*String selectedItemPatient = tvHomePatientFilter.getText().toString();
        String selectedItemPatientId = patientMap.get(tvHomePatientFilter.getText().toString());
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedItemPatient", selectedItemPatient);
        editor.putString("selectedItemPatientId", selectedItemPatientId);
        editor.apply();*/
    }

    //-----------------------------------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        thisFragmentContext = requireContext();
        context = getContext();

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        // To restore the Patient Filter
        selectedPatient = "";
        selectedPatientId = "";
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
            selectedPatient = sharedPreferences.getString("selectedPatient", ""); // default value as empty string
            selectedPatientId = sharedPreferences.getString("selectedPatientId", "");
        }


        btnAdd = view.findViewById(R.id.btnAdd);
        tvFwd = view.findViewById(R.id.tvFwd);
        tvBwd = view.findViewById(R.id.tvBwd);

        taskList = new ArrayList<>();
        originalTaskList = new ArrayList<>();
        taskInDay = new ArrayList<>();



        //------------------------------------------------------------------
        //Load user name and uer id
        tvUserName = view.findViewById(R.id.tvUserName);

        // Since we are using Fragment which does not have its own Intent.
        // We need to access the intent from the host activity that contains the Fragment
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            userId = bundle.getString("userId");
            userName = bundle.getString("userName");
            userEmail = bundle.getString("userEmail");
            userRole = bundle.getString("userRole");

            tvUserName.setText(userName);
            if (userRole.equals("Doctor"))
                selectable = true;

            if (userRole.equals("Patient")) {
                btnAdd.setVisibility(View.GONE);    // hide the button and make it not occupy any space
                tvFwd.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_primary2));
                tvBwd.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_primary2));
            }
        }


//--------------------------------------------------------------------------------------------------------------------
        // get Patient list from Firestore
        // and set the Patient drop-down menu
        tvHomePatientFilter = view.findViewById(R.id.tvHomePatientFilter);
        TextInputLayout txtHomeInputPatient = view.findViewById(R.id.txtHomeInputPatient);

        if (!selectable) {
            // if the user is a patient, show the user's name disable the drop-down menu
            // set it to invisible and remove it from the layout so that it will not occupy space
            // if use setVisibility(View.INVISIBLE), it will still occupy space
            tvHomePatientFilter.setVisibility(View.GONE);
            txtHomeInputPatient.setVisibility(View.GONE);
            tvHomePatientFilter.setText(userName);
            tvHomePatientFilter.setFocusable(false);
            tvHomePatientFilter.setFocusableInTouchMode(false);
            tvHomePatientFilter.setInputType(InputType.TYPE_NULL);
            tvHomePatientFilter.setOnClickListener(null);
            tvHomePatientFilter.setOnTouchListener(null);
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
                                    tvHomePatientFilter.setAdapter(patientAdapter);
                                    //tvHomePatientFilter.setText(patientAdapter.getItem(0), false);
                                    if (selectedPatient != null) {
                                        if (!selectedPatient.equals(""))    // if empty
                                            tvHomePatientFilter.setText(selectedPatient, false);
                                    }
                                    tvHomePatientFilter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            String patient = parent.getItemAtPosition(position).toString();
                                            String patientId = patientMap.get(tvHomePatientFilter.getText().toString());

                                            //String selectedItem = tvHomePatientFilter.getText().toString();
                                            //String selectedPatientId = patientMap.get(tvHomePatientFilter.getText().toString());
                                            SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("selectedPatient", patient);
                                            editor.putString("selectedPatientId", patientId);
                                            editor.apply();

                                            LoadDataFromDB();
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(thisFragmentContext, "Error getting user data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            //patientDbReady = true;

        }
//--------------------------------------------------------------------------------------------------------------------
        //set calendar
        rvCalendar = view.findViewById(R.id.rvCalendar);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        tvPendingTaskTitle = view.findViewById(R.id.tvPendingTaskTitle);
        rvDashboardTaskList = view.findViewById(R.id.rvDashboardTaskList);

        selectedDate = LocalDate.now();
        //setMonthView();


        LoadDataFromDB();   // load data from Firestore

        MyCalendarAdapter adapter = new MyCalendarAdapter();
        adapter.resetBackgroundColors();

        tvFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMonthAction(v);
            }
        });

        tvBwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousMonthAction(v);
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a bundle to pass task details to other Fragment
                String dateString;
                if (clickedDate == null) {
                    dateString = CalculateDate.monthFromDate(selectedDate) + "-"
                            + dayFromDate(selectedDate) + "-"
                            + CalculateDate.yearFromDate(selectedDate);
                } else {
                    dateString = clickedDate;
                }

                Bundle bundle = new Bundle();
                bundle.putString("clickedDate", dateString);
                bundle.putString("selectedPatient", tvHomePatientFilter.getText().toString());
                bundle.putString("newOrUpdate", "new");

                // Navigate to other Fragment
                AddItemFragment addItemFragment = new AddItemFragment();
                //ListFragment addItemFragment = new ListFragment();
                addItemFragment.setArguments(bundle);

                // Use FragmentManager to replace the current fragment with AddItemFragment
                FragmentManager fragmentManager = (requireActivity().getSupportFragmentManager());
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_layout, addItemFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }
    // end of onCreateView()
//--------------------------------------------------------------------------------------------------------------------
    // set the recycler view to display the calendar
    private void setMonthView() {

        //define sources
        daysInMonth = daysInMonthArray(selectedDate);
        dateClass = new DateClass(yearFromDate(selectedDate), monthFromDate(selectedDate), daysInMonth);

        // set the title of the calendar
        tvMonthYear.setText(monthYearFromDate(selectedDate));

        // feed the data to recycler view
        calendarAdapter = new MyCalendarAdapter(dateClass, taskInDay, thisFragmentContext);
        calendarLayoutManager = new GridLayoutManager(thisFragmentContext.getApplicationContext(), 7);
        rvCalendar.setLayoutManager(calendarLayoutManager);
        rvCalendar.setAdapter(calendarAdapter);
        calendarAdapter.notifyDataSetChanged();
        ((MyCalendarAdapter) calendarAdapter).setOnItemClickListener(this);

        tvPendingTaskTitle.setText("Tasks on: " + CalculateDate.monthFromDate(selectedDate) + "-"
                + dayFromDate(selectedDate) + "-"
                + CalculateDate.yearFromDate(selectedDate));
    }

    // get the days in a month and convert them to an array
    private ArrayList<String> daysInMonthArray(LocalDate date) {

        ArrayList<String> daysInMonthArray = new ArrayList<>();

        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
                TaskCategoryClass task = new TaskCategoryClass();
                taskInDay.add(task);
            }
            else {
                int day = i - dayOfWeek;
                daysInMonthArray.add(String.valueOf(day));

                // Insert no. of task
                TaskCategoryClass task = new TaskCategoryClass(appointment[day], medicine[day], workout[day], others[day] , Planning[day] , Development[day] , Testing[day]);
                taskInDay.add(task);
            }
        }

        // delete empty row of the calendar
        if (daysInMonthArray.get(35) == "") {   // delete end portion
            for (int i = 41; i >= 35; i--) {
                daysInMonthArray.remove(i);
                taskInDay.remove(i);
            }
        }

        if (daysInMonthArray.get(6) == "") {   // delete start portion
            for (int i = 6; i >= 0; i--) {
                daysInMonthArray.remove(i);
                taskInDay.remove(i);
            }
        }
        return daysInMonthArray;
    }

    // subtract the month by 1 when the user click backward button of the calendar
    public void previousMonthAction(View view) {
        selectedDate = selectedDate.minusMonths(1);
        LoadDataFromDB();   // load data from Firestore
        //setMonthView();
    }

    // add the month by 1 when the user click forward button of the calendar
    public void nextMonthAction(View view) {
        selectedDate = selectedDate.plusMonths(1);
        LoadDataFromDB();   // load data from Firestore
        //setMonthView();
    }

    // do something if the user click any one of the day on the calendar
    @Override
    public void onItemClick(String day) {
        //Toast.makeText(thisFragmentContext, "Selected Day: " + day + " " + monthFromDate(selectedDate) + " " + yearFromDate(selectedDate), Toast.LENGTH_SHORT).show();
        setTaskList(Integer.parseInt(day));
        tvPendingTaskTitle.setText("Activities on: " + CalculateDate.monthFromDate(selectedDate) + "-"
                                                        + day + "-"
                                                        + CalculateDate.yearFromDate(selectedDate));

        // update the user clicked date
        clickedDate = CalculateDate.monthFromDate(selectedDate) + "-"
                + day + "-"
                + CalculateDate.yearFromDate(selectedDate);

    }
//------------------------------------------------------------------
    //set the content of Task List
    private void setTaskList(int day) {
        // sources is 'taskList'
        ArrayList<TaskClass> selectedDayTaskList = new ArrayList<>();

        // Iterate through the arraylist to search for the task in the target date
        for (TaskClass eachTask: taskList) {
            if (eachTask.getDay() == day) {
                selectedDayTaskList.add(eachTask);
            }
        }

        //taskListAdapter = new MyTaskListAdapter(taskList, thisFragmentContext);   // for displaying all data in the selected month, testing only

        taskListAdapter = new MyTaskListAdapter(selectedDayTaskList, userRole, selectedPatientId, thisFragmentContext);
        taskListLayoutManager = new LinearLayoutManager(thisFragmentContext);
        rvDashboardTaskList.setLayoutManager(taskListLayoutManager);
        rvDashboardTaskList.setAdapter(taskListAdapter);
        taskListAdapter.notifyDataSetChanged();
    }

//------------------------------------------------------------------
    private void LoadDataFromDB() {
        // Load all data in the selected month for the logged in user

        int year = selectedDate.getYear();
        int month = selectedDate.getMonthValue();
        int day = selectedDate.getDayOfMonth();


        //selectedPatientId = patientMap.get(tvHomePatientFilter.getText().toString());
        //String status = "Pending";
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
            selectedPatient = sharedPreferences.getString("selectedPatient", ""); // default value as empty string
            selectedPatientId = sharedPreferences.getString("selectedPatientId", "");
        }


        // Clear the lists first
        taskList.clear();
        originalTaskList.clear();
        taskInDay.clear();
        Query query;

        if (userRole.equals("Doctor")) {    //doctor can see all the data
                query = taskCollection
                        .whereEqualTo("year", year)
                        .whereEqualTo("month", month)
                        //.whereEqualTo("day", day)
                        //.whereEqualTo("patientId", patientList.get(selectedPatientIndex).getUserId())
                        .whereEqualTo("patientId", selectedPatientId)
                        //.whereEqualTo("status", status)
                        .orderBy("day", Query.Direction.ASCENDING)
                        .orderBy("hour", Query.Direction.ASCENDING)
                        .orderBy("minute", Query.Direction.ASCENDING);
                //.orderBy("day");
                //.whereEqualTo("category", category);


        } else {    // if the role is a patient, who can only view their own data
            // need to create index in Firestore first, click the link in the error msg
            String patientId = userId;
            query = taskCollection
                    .whereEqualTo("year", year)
                    .whereEqualTo("month", month)
                    //.whereEqualTo("day", day)
                    .whereEqualTo("patientId", patientId)
                    //.whereEqualTo("status", status)
                    .orderBy("day", Query.Direction.ASCENDING)
                    .orderBy("hour", Query.Direction.ASCENDING)
                    .orderBy("minute", Query.Direction.ASCENDING);
            //.orderBy("day");
            //.whereEqualTo("category", category);
        }

        // Execute the query to get the matching documents
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        TaskClass eachTask = document.toObject(TaskClass.class);
                        eachTask.setId(document.getId());       // To get document id for further update or delete
                        //originalTaskList.add(eachTask);
                        taskList.add(eachTask);
                    }

                    LoadDataToCalendar(day);

                    //create alarm
                    if (userRole.equals("Patient")) {
                        scheduleAlarm();
                    }

                } else {
                    // Display the error
                    Toast.makeText(thisFragmentContext, task.getException().toString(), Toast.LENGTH_LONG).show();
                    Log.d("Firestore error", task.getException().toString());
                }
            }
        });

    } // end of LoadDataFromDB()

//------------------------------------------------------------------
    private void LoadDataToCalendar(int selectedDay) {
        // Initialize the Category arrays
        for (int i = 0; i <= 31; i++) {
            appointment[i] = 0;
            medicine[i] = 0;
            workout[i] = 0;
            others[i] = 0;
            Planning[i]= 0;
            Development[i]= 0;
            Testing[i]= 0;
        }

        // Loop through the downloaded data and count the number of category for each day
        for (int i = 0; i < taskList.size(); i++) {
            int day = taskList.get(i).getDay();
            String status = taskList.get(i).getCategory();
            if (status.equals("Appointment"))
                appointment[day]++;
            if (status.equals("Medicine"))
                medicine[day]++;
            if (status.equals("Workout"))
                workout[day]++;
            if (status.equals("Others"))
                others[day]++;
            if (status.equals("Planning"))
                Planning[day]++;
            if (status.equals("Development"))
                Development[day]++;
            if (status.equals("Testing"))
                Testing[day]++;
        }

        // Set the calendar
        setMonthView();

        // Set the task list that under the calendar
        setTaskList(selectedDay);
    }
//------------------------------------------------------------------



    private void scheduleAlarm() {
        // loop through the taskList to see if alarm was set for each task
        for (TaskClass eachTask : taskList) {
            if (eachTask.isSetAlarm() == false) {       //check if alarm was set for the task. If no, set alarm
                AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(requireContext(), MyNotificationReceiver.class); // Replace with your BroadcastReceiver class
                long notificationId = System.currentTimeMillis(); // Use a timestamp as a unique ID

                Bundle bundle = new Bundle();
                bundle.putString("msg_title", eachTask.getTaskTitle());
                bundle.putString("msg_description", eachTask.getDescription());
                bundle.putString("msg_category", eachTask.getCategory());
                String hourString = String.format(Locale.getDefault(), "%02d", eachTask.getHour());
                String minuteString = String.format(Locale.getDefault(), "%02d", eachTask.getMinute());
                bundle.putString("msg_time", hourString + ":" + minuteString);
                bundle.putInt("notification_id", (int) notificationId); // Use a unique ID for each notification
                intent.putExtras(bundle);

               /* intent.putExtra("msg", eachTask.getTaskTitle());
                intent.putExtra("notification_id", (int) notificationId); // Use a unique ID for each notification*/

                PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                // Set the desired time for the notification (replace with your desired time logic)
                calendar.set(Calendar.YEAR, eachTask.getYear());
                calendar.set(Calendar.MONTH, eachTask.getMonth() - 1);  //Note: Months are zero-based (0 for January, 1 for February, etc.)
                calendar.set(Calendar.DAY_OF_MONTH, eachTask.getDay());
                calendar.set(Calendar.HOUR_OF_DAY, eachTask.getHour());
                calendar.set(Calendar.MINUTE, eachTask.getMinute());
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                // Create an AlarmClockInfo object
                AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent);

                // Set the alarm using setAlarmClock()
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);

                //update the field in database
                eachTask.setSetAlarm(true);     // mark the field to indicate alarm was set for this task
                Map<String, Object> updatedData = new HashMap<>();
                updatedData.put("setAlarm", true);
                taskCollection.document(eachTask.getId())
                        .update(updatedData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getActivity(), "Alarm set for activity: " + eachTask.getTaskTitle(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), "Error on updating task:" + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }


    }







}