package com.example.taskmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.taskmanager.CustomerClass.DateString;
import com.example.taskmanager.CustomerClass.MsgClass;
import com.example.taskmanager.CustomerClass.TimeString;
import com.example.taskmanager.CustomerClass.UserClass;
import com.example.taskmanager.TaskList.TaskClass;
import com.example.taskmanager.Utility.CalculateDate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class AddItemFragment extends Fragment {
    Context thisFragmentContext, context;
    public String userId, userRole;

    String selectedDate;

//---------------------------------------------------------------
    // connection to Firebase Realtime database
    FirebaseDatabase realtime_db = FirebaseDatabase.getInstance();

    // get a reference to a specific node in the db
    //DatabaseReference myRef = realtime_db.getReference("message");

    // connection to Firebase Firestore database
    private FirebaseFirestore firestore_db = FirebaseFirestore.getInstance();
    private CollectionReference taskCollection = firestore_db.collection("Tasks");  // tasks collection
    private CollectionReference userCollection = firestore_db.collection("Users");  // users collection

    String documentId;

//---------------------------------------------------------------
    // Setup Patient drop-down menu

    ArrayList<UserClass> patientList = new ArrayList<>();  // to store patient list get from Firestore db
    ArrayList<String> patientNameList = new ArrayList<>();  // to store patient name

    Map<String, String> patientMap = new HashMap<>();
    AutoCompleteTextView tvSelectPatient;
    ArrayAdapter<String> patientAdapter;

    int selectedPatientIndex;   // to store the index of drop-down menu

//---------------------------------------------------------------
    // Setup Category drop-down menu
    String[] categories = {"Planning", "Development", "Testing", "Others"};
    AutoCompleteTextView tvCategory;
    ArrayAdapter<String> categoryAdapter;

    String selectedCategory;

//---------------------------------------------------------------
    EditText etInputTaskTitle, etInputDescription;
    Button  btnSubmit, btnDelete;

    TextView tvInputTime, tvInputDate;
    TextInputLayout txtInputPatient;
    int inputYear, inputMonth, inputDay, inputHour, inputMinute;

    TaskClass taskDetail;   // To save the task detail that passed from the Pending task list

    Boolean isUpdate = false;   // To indicate whether the status is update or read only
    Boolean isNewTask = false;  // To indicate whether it is a new task

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        thisFragmentContext = requireContext();



        //------------------------------------------------------------------
        //Load user name and uer id
        // Since we are using Fragment which does not have its own Intent.
        // We need to us getActivity() to access the intent from the host activity that contains the Fragment
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            userId = bundle.getString("userId");
            //userName = bundle.getString("userName");
            //userEmail = bundle.getString("userEmail");
            userRole = bundle.getString("userRole");

            if (userRole.equals("Doctor")) {
                thisFragmentContext.setTheme(R.style.Doctor_Theme);
            }
        }

        View view = inflater.inflate(R.layout.fragment_add_item, container, false);


        etInputTaskTitle = view.findViewById(R.id.etInputTaskTitle);
        etInputDescription = view.findViewById(R.id.etInputDescription);
        btnSubmit =view.findViewById(R.id.btnSubmit);
        btnDelete = view.findViewById(R.id.btnDelete);
        tvInputDate = view.findViewById(R.id.tvInputDate);
        tvInputTime = view.findViewById(R.id.tvInputTime);
        tvCategory = view.findViewById(R.id.tvCategoryFilter);
        tvSelectPatient = view.findViewById(R.id.tvHomePatientFilter);
        txtInputPatient = view.findViewById(R.id.txtInputPatient);

//------------------------------------------------------------------
        //Load user name and user id
        //TextView tvHeading = view.findViewById(R.id.tvHeading);

        // Since we are using Fragment which does not have its own Intent.
        // We need to us getActivity() to access the intent from the host activity that contains the Fragment
        //Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            if (userRole.equals("Patient"))
                isUpdate = false;
            else
                isUpdate = true;
        }

        if (isUpdate) {
            // disable Patient drop-down menu
            tvSelectPatient.setFocusable(false);
            tvSelectPatient.setFocusableInTouchMode(false);
            tvSelectPatient.setInputType(InputType.TYPE_NULL);
            tvSelectPatient.setOnClickListener(null);
            tvSelectPatient.setOnTouchListener(null);
        }

        if (!isUpdate) {    // Read only mode (i.e. Patient)
            //if (userRole.equals("Patient")) {
            // disable the submit button
            //btnSubmit.setEnabled(false);
            btnSubmit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);

            // set all EditText to read only
            etInputTaskTitle.setFocusable(false);
            etInputTaskTitle.setFocusableInTouchMode(false);
            etInputDescription.setFocusable(false);
            etInputDescription.setFocusableInTouchMode(false);

            tvSelectPatient.setFocusable(false);
            tvSelectPatient.setFocusableInTouchMode(false);
            tvSelectPatient.setInputType(InputType.TYPE_NULL);
            tvSelectPatient.setOnClickListener(null);
            tvSelectPatient.setOnTouchListener(null);
                /*txtInputPatient.setFocusable(false);
                txtInputPatient.setFocusableInTouchMode(false);
                txtInputPatient.setOnClickListener(null);
                txtInputPatient.setOnTouchListener(null);*/

            tvCategory.setFocusable(false);
            tvCategory.setFocusableInTouchMode(false);
            tvCategory.setInputType(InputType.TYPE_NULL);
            tvCategory.setOnClickListener(null);
            tvCategory.setOnTouchListener(null);

            tvInputDate.setFocusable(false);
            tvInputDate.setFocusableInTouchMode(false);
            tvInputDate.setOnClickListener(null);
            tvInputDate.setOnTouchListener(null);

            tvInputTime.setFocusable(false);
            tvInputTime.setFocusableInTouchMode(false);
            tvInputTime.setOnClickListener(null);
            tvInputTime.setOnTouchListener(null);
        }

        // Load data transferred from Home Fragment
        Bundle bundle_fragment = getArguments();
        if (bundle_fragment != null) {
            if (bundle_fragment.getString("newOrUpdate").equals("new")) {   // if create a new task
                isNewTask = true;   // if no bundle transferred received, treat it as a new task
                btnSubmit.setText("Add");
                selectedDate = bundle_fragment.getString("clickedDate");    // get user clicked date in HomeFragment
                tvInputDate.setText(selectedDate);



                String selectedPatient = bundle_fragment.getString("selectedPatient");  // get user selected Patient in HomeFragment
                if (!selectedPatient.equals("All")) {
                    tvSelectPatient.setText(selectedPatient);
                }

                btnDelete.setVisibility(View.GONE);
            }
            if (bundle_fragment.getString("newOrUpdate").equals("update")) {   // if update a new task
                taskDetail = bundle_fragment.getParcelable("taskDetails");
                documentId = taskDetail.getId();

                isNewTask = false;
                btnSubmit.setText("Update");

                etInputTaskTitle.setText(taskDetail.getTaskTitle());
                etInputDescription.setText(taskDetail.getDescription());
                tvSelectPatient.setText(taskDetail.getPatientName());
                tvCategory.setText(taskDetail.getCategory());
                tvInputDate.setText(taskDetail.getMonth() + "-" + taskDetail.getDay() + "-" + taskDetail.getYear());
                String hourString = String.format(Locale.getDefault(), "%02d", taskDetail.getHour());
                String minuteString = String.format(Locale.getDefault(), "%02d", taskDetail.getMinute());
                tvInputTime.setText(hourString +":" + minuteString);

                //tvInputDate = view.findViewById(R.id.tvInputDate);
                //tvInputTime = view.findViewById(R.id.tvInputTime);
            }

        }
//------------------------------------------------------------------
        // get Patient list from Firestore
        // and set the Patient drop-down menu
        if (isUpdate == true || isNewTask == true) {

            userCollection
                    .whereEqualTo("userRole", "Patient")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
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
                                    tvSelectPatient.setAdapter(patientAdapter);

                                    tvSelectPatient.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            //String item = parent.getItemAtPosition(position).toString();
                                            selectedPatientIndex = position;
                                        /*Toast.makeText(thisFragmentContext, "Position: " + selectedPatientIndex + "; Name: " + patientList.get(selectedPatientIndex).getUserName() +
                                                "; Email: " + patientList.get(selectedPatientIndex).getUserEmail(), Toast.LENGTH_SHORT).show();*/

                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(thisFragmentContext, "Error getting user data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


        }
//------------------------------------------------------------------
        // Set Date picker onClick listener
        if (isUpdate == true || isNewTask == true) {
            tvInputDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long defaultDateInMillis;
                    if (isUpdate && !isNewTask) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(taskDetail.getYear(), taskDetail.getMonth() - 1, taskDetail.getDay());
                        defaultDateInMillis = calendar.getTimeInMillis();
                    } else {
                        defaultDateInMillis = MaterialDatePicker.todayInUtcMilliseconds();
                    }

                    // Set up the Material 3 Date Picker
                    MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select Date")
                            .setSelection(defaultDateInMillis)
                            .build();

                    materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                        // Use UTC time zone for date formatting to match the date picker's default behavior
                        SimpleDateFormat sdf = new SimpleDateFormat("M-dd-yyyy", Locale.getDefault());
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String formattedDate = sdf.format(new Date(selection));
                        tvInputDate.setText(formattedDate);
                    });

                    // Show the Material 3 Date Picker
                    materialDatePicker.show(getActivity().getSupportFragmentManager(), "tag");
                }
            });
            //Toast.makeText(thisFragmentContext, inputYear + "-" + inputMonth + "-" + inputDay, Toast.LENGTH_LONG).show();
        }

//------------------------------------------------------------------
        // Set Time picker onClick listener
        if (isUpdate == true || isNewTask == true) {
            tvInputTime.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int hour, minute;
                    if (isUpdate && !isNewTask) {   // if update, not new task
                        hour = taskDetail.getHour();
                        minute = taskDetail.getMinute();
                    } else {    // if a new task
                        // Get the current time
                        Calendar calendar = Calendar.getInstance();

                        // Get the current hour and minute
                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY); // 24-hour format
                        int currentMinute = calendar.get(Calendar.MINUTE);

                        hour = currentHour;
                        minute = currentMinute;
                    }
                    MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .setHour(hour)
                            .setMinute(minute)
                            .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                            .setTitleText("Pick Time (24 hrs format)")
                            .build();
                    timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String hourString = String.format(Locale.getDefault(), "%02d", timePicker.getHour());
                            String minuteString = String.format(Locale.getDefault(), "%02d", timePicker.getMinute());
                            tvInputTime.setText(hourString + ":" + minuteString);
                        }
                    });
                    timePicker.show(getActivity().getSupportFragmentManager(), "tag");

                }
            });
        }

//------------------------------------------------------------------
        // Add categories to the Category drop-down menu
        if (isUpdate == true || isNewTask == true) {
            // Use the new dropdown_item_layout.xml for the adapter
            categoryAdapter = new ArrayAdapter<String>(thisFragmentContext, R.layout.dropdown_item_layout, categories);

            // Specify the layout resource for dropdown items
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            tvCategory.setAdapter(categoryAdapter);

            tvCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedCategory =  parent.getItemAtPosition(position).toString();
                    //Toast.makeText(thisFragmentContext, selectedCategory, Toast.LENGTH_SHORT).show();
                }
            });
        }
//------------------------------------------------------------------
        // Set submit button onClick listener
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    AddData();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog.Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(thisFragmentContext);
                builder.setTitle("Confirm Delete");
                builder.setMessage("Are you sure you want to delete this item?");

                // Add buttons for confirming or cancelling the delete operation
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User confirmed the delete operation
                        taskCollection.document(documentId)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // Document successfully deleted
                                        String hourString = String.format(Locale.getDefault(), "%02d", taskDetail.getHour());
                                        String minuteString = String.format(Locale.getDefault(), "%02d", taskDetail.getMinute());
                                        String msg = taskDetail.getTaskTitle() + " on " +
                                                        taskDetail.getMonth() + "-" + taskDetail.getDay() + "-" + taskDetail.getYear() +
                                                        "(" + hourString + ":" + minuteString + ")";
                                        //String patientId = patientList.get(selectedPatientIndex).getUserId();
                                        String patientId = patientMap.get(tvSelectPatient.getText().toString());
                                        writeNotification("Activity deleted: ", msg, documentId, patientId);
                                        Toast.makeText(thisFragmentContext, "Activity deleted" + msg, Toast.LENGTH_LONG).show();

                                        // return to Home Fragment
                                        HomeFragment homeFragment = new HomeFragment();

                                        // Use FragmentManager to replace the current fragment with AddItemFragment
                                        FragmentManager fragmentManager = (requireActivity().getSupportFragmentManager());
                                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                                        transaction.replace(R.id.frame_layout, homeFragment);
                                        //transaction.addToBackStack(null);     // do not add to the back stack
                                        transaction.commit();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(thisFragmentContext, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User cancelled the delete operation, do nothing
                    }
                });

                // Show the AlertDialog
                builder.create().show();
            }
        });

        return view;
    }
    //end of onCreateView()
//--------------------------------------------------------------------------------------------------------------------
    private void AddData() {
        // Add data to database
        //public TaskClass(String taskTitle, String doctorId, String patientName, String patientEmail,
        //        String patientId, String description, String category, String status,
        //int year, int month, int day, int hour, int minute) {
        //String userName = etSignupName.getText().toString().trim();

        String taskTitle = etInputTaskTitle.getText().toString().trim();
        String doctorId = userId;
        //String patientName = patientList.get(selectedPatientIndex).getUserName();
        String patientName = tvSelectPatient.getText().toString();
        String patientEmail = patientList.get(selectedPatientIndex).getUserEmail();
        //String patientId = patientList.get(selectedPatientIndex).getUserId();

        String description = etInputDescription.getText().toString().trim();
        //String category = selectedCategory;
        String category = tvCategory.getText().toString();

        // use a HashMap to store patient name and its associated patientId



        // Check if the Text boxes are empty
        if (taskTitle.isEmpty()) {
            etInputTaskTitle.setError("Title cannot be empty");
            return;
        }

        if (tvSelectPatient.getText().toString().isEmpty()) {
            tvSelectPatient.setError("Patient cannot be empty");
            return;
        }

        if (tvInputDate.getText().toString().isEmpty()) {
            tvInputDate.setError("Date cannot be empty");
            return;
        }

        if (tvInputTime.getText().toString().isEmpty()) {
            tvInputTime.setError("Time cannot be empty");
            return;
        }

        if (category.isEmpty()) {
            tvCategory.setError("Category cannot be empty");
            return;
        }

        DateString dateString = CalculateDate.getDateFromString(tvInputDate.getText().toString());
        inputYear = dateString.getYear();
        inputMonth = dateString.getMonth();
        inputDay = dateString.getDay();

        TimeString timeString = CalculateDate.getTimeFromString(tvInputTime.getText().toString());
        inputHour = timeString.getHour();
        inputMinute = timeString.getMinute();
        String status = "Pending";
        boolean setAlarm = false;

        String patientId = patientMap.get(tvSelectPatient.getText().toString());

        if (isNewTask) {        // create a new task
            TaskClass newTask = new TaskClass(taskTitle, doctorId, patientName, patientEmail, patientId,
                    description, category, status, inputYear, inputMonth, inputDay, inputHour, inputMinute, setAlarm);

            taskCollection.add(newTask)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            writeNotification("New activity added", taskTitle, taskCollection.getId(), patientId);
                            //deleteUsersWithUsernames("James");

                            Toast.makeText(getActivity(), "Activity add: " + taskTitle + " on " + tvInputDate.getText(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {    // update existing task
            // Create a Map with the updated data
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("taskTitle", taskTitle);
            updatedData.put("description", description);
            updatedData.put("category", category);
            updatedData.put("year", inputYear);
            updatedData.put("month", inputMonth);
            updatedData.put("day", inputDay);
            updatedData.put("hour", inputHour);
            updatedData.put("minute", inputMinute);
            updatedData.put("setAlarm", false);

            // Update the document in Firestore
            taskCollection.document(documentId)
                    .update(updatedData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        String msg = taskTitle + " on " + tvInputDate.getText();
                        @Override
                        public void onSuccess(Void unused) {
                            writeNotification("Activity updated", msg, documentId, patientId);
                            Toast.makeText(getActivity(), "Activity updated: " + msg, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Error on updating task:" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
        // Redirect to Home Fragment
        HomeFragment homeFragment = new HomeFragment();
        // Use FragmentManager to replace the current fragment with AddItemFragment
        FragmentManager fragmentManager = (getActivity().getSupportFragmentManager());
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, homeFragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    public void writeNotification(String title, String msg, String documentId, String patientId) {
        // connection to Firebase Realtime database
        FirebaseDatabase realtime_db = FirebaseDatabase.getInstance();
        long timeStamp = System.currentTimeMillis(); // Use a timestamp as a unique ID
        MsgClass realtimeMsg = new MsgClass(title, msg, documentId, timeStamp);
        DatabaseReference myRef = realtime_db.getReference(patientId);
        //Toast.makeText(thisFragmentContext, "write to " + patientId, Toast.LENGTH_SHORT).show();
        myRef.setValue(realtimeMsg);
    }

    //--------------------------------

    private void deleteUsersWithUsernames(String... usernames) {
        for (String username : usernames) {
            userCollection
                    .whereEqualTo("userName", username)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Supprimer l'utilisateur
                                    userCollection.document(document.getId()).delete();
                                }
                            }
                        }
                    });
        }
    }




}

