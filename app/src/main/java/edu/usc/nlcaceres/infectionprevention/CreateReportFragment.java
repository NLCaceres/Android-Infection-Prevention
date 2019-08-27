package edu.usc.nlcaceres.infectionprevention;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

public class CreateReportFragment extends Fragment {

    private Spinner mFacilitySpinner;
    private ArrayList<String> facilityNames;

    private Spinner mUnitSpinner;
    private ArrayList<String> unitNames;

    private Spinner mRoomSpinner;
    private ArrayList<String> roomNums;

    private Spinner mOccupationSpinner;
    private ArrayList<String> occupationList;

    private Spinner mServiceSpinner;
    private ArrayList<String> serviceList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.create_report_fragment_one, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // 1st METHOD TO SET UP SPINNER: string array res

        mFacilitySpinner = view.findViewById(R.id.facilitySpinner);
        // Set the adapter context, dataset, and layout look
        ArrayAdapter<CharSequence> facilityAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.facility_spinner, android.R.layout.simple_spinner_item);
        facilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFacilitySpinner.setAdapter(facilityAdapter);
        mFacilitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Used to determine the units in a particular campus
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });


        // 2nd METHOD: Create an array programmatically

        mUnitSpinner = view.findViewById(R.id.unitSpinner);
        unitNames= new ArrayList<>();
        unitNames.add("Hi");
        unitNames.add("Hi");
        unitNames.add("hi");
        // context, layout, dataset
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, unitNames);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUnitSpinner.setAdapter(unitAdapter);
        mUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Used to determine the rooms in a particular unit
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // METHOD 2

        mRoomSpinner = view.findViewById(R.id.roomSpinner);
        roomNums = new ArrayList<>();
        roomNums.add("123");
        roomNums.add("124");
        roomNums.add("125");
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, roomNums);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRoomSpinner.setAdapter(roomAdapter);
        mRoomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Naturally all of this should pour into a class
                // This won't affect too much afterward
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // METHOD 1

        mOccupationSpinner = view.findViewById(R.id.occupationSpinner);
        ArrayAdapter<CharSequence> occupationAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.occupation_spinner, android.R.layout.simple_spinner_item);
        occupationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mOccupationSpinner.setAdapter(occupationAdapter);
        mOccupationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // This will be used to add in the service/discipline based on general occupation
                // It would work as nurse to give RN, LVN, etc.
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });


        // METHOD 2

        mServiceSpinner = view.findViewById(R.id.serviceSpinner);
        serviceList = new ArrayList<>();
        serviceList.add("RN");
        serviceList.add("Anesthesiologist");
        serviceList.add("LVN");
        ArrayAdapter<String> serviceAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, serviceList);
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mServiceSpinner.setAdapter(serviceAdapter);
        mServiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Last var of the report class
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
}
