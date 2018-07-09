package api

import (
	"net/http"
	"strconv"
	"strings"
	"testing"
	"time"

	"github.com/TravisS25/httputil/confutil"

	"github.com/TravisS25/httputil/apiutil"
	"github.com/TravisS25/inventory-tracking/src/server/config"
	"github.com/TravisS25/inventory-tracking/src/server/forms"
	"github.com/TravisS25/inventory-tracking/src/server/models"
)

// ------------------- INTEGRATION TESTING ---------------------

func TestMachineUploadIntegrationTest(t *testing.T) {
	testCase1 := apiutil.TestCase{
		TestName:   "machineUpload1",
		Method:     "GET",
		RequestURL: config.RouterPaths["machineUpload"],
		//RequestURL:     "/api/machine/upload/",
		ExpectedStatus: http.StatusOK,
		ExpectedBody:   "",
		Handler:        IntegrationTestRouter,
	}

	machines := []forms.MachineForm{
		forms.MachineForm{
			RoomID:          1,
			MachineStatusID: 1,
			MachineName:     "Machine1",
		},
		forms.MachineForm{
			RoomID:          1,
			MachineStatusID: 1,
			MachineName:     "Machine2",
		},
	}

	testCase2 := testCase1
	testCase2.TestName = "machineUpload2"
	testCase2.Method = "POST"
	testCase2.Form = machines

	testCase3 := testCase2
	testCase3.TestName = "machineUpload3"
	testCase3.ExpectedStatus = http.StatusNotAcceptable
	testCase3.ExpectedBody = "Can't have empty list"
	testCase3.Form = []forms.MachineForm{}

	testCase4 := testCase3
	testCase4.TestName = "machineUpload4"
	testCase4.ExpectedBody = ""
	testCase4.Form = []forms.MachineForm{
		forms.MachineForm{},
	}

	apiutil.RunTestCases(t, []apiutil.TestCase{
		testCase1,
		testCase2,
		testCase3,
		testCase4,
	})

	for _, v := range machines {
		machine, err := models.QueryMachine(
			TestDB,
			"select * from machine where machine_name = $1;",
			v.MachineName,
		)

		if err != nil {
			t.Errorf("Could not delete machine %s", v.MachineName)
		}

		err = machine.Delete(TestDB)

		if err != nil {
			t.Errorf("Could not delete machine %s", v.MachineName)
		}
	}
}

func TestMachineDetailsIntegrationTest(t *testing.T) {
	insertMachine := models.Machine{
		MachineName:     "MachineDetails",
		MachineStatusID: 1,
		RoomID:          1,
		ScannedTime:     time.Now().UTC().Format(confutil.DateTimeLayout),
	}
	err := insertMachine.Insert(TestDB)

	if err != nil {
		t.Fatal("Unable to insert machine")
	}

	machineID := strconv.Itoa(insertMachine.ID)
	url := strings.Replace(config.RouterPaths["machineDetails"], "{id:[0-9]+}", machineID, 1)
	testCase1 := apiutil.TestCase{
		TestName: "machineDetails1",
		Method:   "GET",
		//RequestURL: "/api/machine/details/",
		RequestURL:     url,
		ExpectedStatus: http.StatusOK,
		ExpectedBody:   "",
		Handler:        IntegrationTestRouter,
	}

	apiutil.RunTestCases(t, []apiutil.TestCase{
		testCase1,
	})

	machine, err := models.QueryMachine(
		TestDB,
		"select * from machine where id = $1;",
		insertMachine.ID,
	)

	if err != nil {
		t.Fatal("Could not find machine")
	}

	err = machine.Delete(TestDB)

	if err != nil {
		t.Fatal("Could not delete machine")
	}
}

func TestMachineEditIntegrationTest(t *testing.T) {
	insertMachine := models.Machine{
		MachineName:     "Machine1",
		MachineStatusID: 1,
		RoomID:          1,
		ScannedTime:     time.Now().UTC().Format(confutil.DateTimeLayout),
	}
	updateMachine := models.Machine{
		MachineName:     "Updated Machine1",
		MachineStatusID: 2,
		RoomID:          2,
	}

	err := insertMachine.Insert(TestDB)

	if err != nil {
		t.Fatal("Could not insert machine")
	}

	url := strings.Replace(config.RouterPaths["machineEdit"], "{id:[0-9]+}", strconv.Itoa(insertMachine.ID), 1)

	testCase1 := apiutil.TestCase{
		TestName:   "machineEdit1",
		Method:     "GET",
		RequestURL: url,
		//RequestURL:     "/api/machine/edit/" + strconv.Itoa(insertMachine.ID) + "/",
		ExpectedStatus: http.StatusOK,
		ExpectedBody:   "",
		Handler:        IntegrationTestRouter,
	}

	testCase2 := testCase1
	testCase2.TestName = "machineEdit2"
	testCase2.Method = "PUT"
	testCase2.ExpectedStatus = http.StatusNotAcceptable
	testCase2.Form = forms.MachineForm{}

	testCase3 := testCase2
	testCase3.TestName = "machineEdit3"
	testCase3.ExpectedStatus = http.StatusOK
	testCase3.Form = updateMachine

	apiutil.RunTestCases(t, []apiutil.TestCase{
		testCase1,
		testCase2,
		testCase3,
	})

	machine, err := models.QueryMachine(
		TestDB,
		"select * from machine where id = $1;",
		insertMachine.ID,
	)

	if err != nil {
		t.Fatal("Could not query machine")
	}

	if machine.MachineName != updateMachine.MachineName {
		t.Errorf("machineName shuold be %s, got %s", updateMachine.MachineName, machine.MachineName)
	}

	if machine.MachineStatusID != updateMachine.MachineStatusID {
		t.Errorf("machineStatusID should be %d; got %d", updateMachine.MachineStatusID, machine.MachineStatusID)
	}

	if machine.RoomID != updateMachine.RoomID {
		t.Errorf("roomID should be %d, got %d;", updateMachine.RoomID, machine.RoomID)
	}

	err = insertMachine.Delete(TestDB)

	if err != nil {
		t.Fatalf("Could not delete machine")
	}
}

func TestMachineSearchIntegrationTest(t *testing.T) {
	url := config.RouterPaths["machineSearch"] + "?take=20&skip=0"

	testCase1 := apiutil.TestCase{
		TestName:       "machineSearch1",
		Method:         "GET",
		RequestURL:     url,
		ExpectedStatus: http.StatusOK,
		ExpectedBody:   "",
		Handler:        IntegrationTestRouter,
	}

	apiutil.RunTestCases(t, []apiutil.TestCase{
		testCase1,
	})
}

func TestMachineSwapIntegrationTest(t *testing.T) {
	oldMachine := models.Machine{
		RoomID:          1,
		MachineStatusID: 1,
		MachineName:     "Old Machine",
		ScannedTime:     time.Now().UTC().Format(confutil.DateTimeLayout),
	}
	newMachine := models.Machine{
		RoomID:          1,
		MachineStatusID: 1,
		MachineName:     "New Machine",
		ScannedTime:     time.Now().UTC().Format(confutil.DateTimeLayout),
	}
	oldInvalidMachine := models.Machine{
		RoomID:          1,
		MachineStatusID: 3,
		MachineName:     "Old Invalid Machine",
		ScannedTime:     time.Now().UTC().Format(confutil.DateTimeLayout),
	}

	err := oldMachine.Insert(TestDB)

	if err != nil {
		t.Fatal("Could not insert old machine")
	}

	err = newMachine.Insert(TestDB)

	if err != nil {
		t.Fatal("Could not insert new machine")
	}

	err = oldInvalidMachine.Insert(TestDB)

	if err != nil {
		t.Fatal("Could not insert old invalid machine")
	}

	oldMachineID := strconv.Itoa(oldMachine.ID)
	oldInvalidMachineID := strconv.Itoa(oldInvalidMachine.ID)
	newMachineID := strconv.Itoa(newMachine.ID)
	tempURL := strings.Replace(config.RouterPaths["machineSwap"], "{oldID:[0-9]+}", oldMachineID, 1)
	url1 := strings.Replace(tempURL, "{newID:[0-9]+}", newMachineID, 1)

	testCase1 := apiutil.TestCase{
		TestName:       "machineSwap1",
		Method:         "GET",
		RequestURL:     url1,
		ExpectedStatus: http.StatusOK,
		Handler:        IntegrationTestRouter,
	}

	testCase2 := testCase1
	testCase2.TestName = "machineSwap2"
	testCase2.Method = "PUT"
	testCase2.ExpectedStatus = http.StatusNotAcceptable
	testCase2.Form = forms.MachineSwapForm{}

	validForm := forms.MachineSwapForm{
		MachineStatusID: 3,
	}

	testCase3 := testCase2
	testCase3.TestName = "machineSwap3"
	testCase3.ExpectedStatus = http.StatusOK
	testCase3.Form = validForm

	tempURL = strings.Replace(config.RouterPaths["machineSwap"], "{oldID:[0-9]+}", oldInvalidMachineID, 1)
	url2 := strings.Replace(tempURL, "{newID:[0-9]+}", newMachineID, 1)

	testCase4 := testCase3
	testCase4.TestName = "machineSwap4"
	testCase4.ExpectedStatus = http.StatusNotAcceptable
	testCase4.RequestURL = url2
	testCase4.ExpectedBody = "Machine you are going to swap for has to be in service"

	apiutil.RunTestCases(t, []apiutil.TestCase{
		testCase1,
		testCase2,
		testCase3,
	})

	newMachineQuery, err := models.QueryMachine(
		TestDB,
		"select * from machine where id = $1;",
		newMachineID,
	)

	oldMachineQuery, err := models.QueryMachine(
		TestDB,
		"select * from machine where id = $1;",
		oldMachineID,
	)

	if newMachineQuery.RoomID != newMachine.RoomID {
		t.Errorf("roomID should be %d; got %d", newMachine.RoomID, newMachineQuery.RoomID)
	}

	if oldMachineQuery.MachineStatusID != validForm.MachineStatusID {
		t.Errorf("machineStatusID should be %d; got %d", validForm.MachineStatusID, oldMachineQuery.MachineStatusID)
	}

	err = oldMachine.Delete(TestDB)

	if err != nil {
		t.Fatal("Could not delete old machine")
	}

	err = newMachine.Delete(TestDB)

	if err != nil {
		t.Fatal("Could not delete new machine")
	}

	err = oldInvalidMachine.Delete(TestDB)

	if err != nil {
		t.Fatal("Could not delete old invalid machine")
	}
}

func TestAllMachineIntegrationTests(t *testing.T) {
	TestMachineSwapIntegrationTest(t)
	TestMachineUploadIntegrationTest(t)
	TestMachineDetailsIntegrationTest(t)
	TestMachineEditIntegrationTest(t)
	TestMachineSearchIntegrationTest(t)
}
