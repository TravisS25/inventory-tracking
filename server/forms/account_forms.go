package forms

import (
	"database/sql"
	"fmt"

	"github.com/TravisS25/httputil/formutil"
	"github.com/go-ozzo/ozzo-validation"
	"github.com/go-ozzo/ozzo-validation/is"
	"golang.org/x/crypto/bcrypt"
)

// EmailForm is used to validate an email exists
type EmailForm struct {
	Email string `json:"email"`
	formutil.FormValidation
}

func (e EmailForm) Validate() error {
	query := `select id from user_profile where email = $1;`
	exists := e.Exists(query, e.Email)

	return validation.ValidateStruct(
		&e,
		validation.Field(
			&e.Email,
			validation.Required.Error(e.RequiredError("Email")),
			e.IsValid(exists).Error(e.ExistError("Email")),
		),
	)
}

type LoginForm struct {
	Email        string `json:"email"`
	Password     string `json:"password"`
	ErrorMessage string `json:"errorMessage"`

	formutil.FormValidation
}

func (l LoginForm) Validate() error {
	var email string
	var password string
	correctInfo := true
	query := "select email, password from user_profile where email = $1;"
	err := l.GetQuerier().QueryRow(query, l.Email).Scan(&email, &password)

	if err == sql.ErrNoRows {
		correctInfo = false
	}

	err = bcrypt.CompareHashAndPassword([]byte(password), []byte(l.Password))

	if err != nil {
		fmt.Printf("compare error: %s", err)
		correctInfo = false
	}

	return validation.ValidateStruct(
		&l,
		validation.Field(
			&l.Email,
			validation.Required.Error(l.RequiredError("Email")),
			is.Email.Error("Enter valid email"),
		),
		validation.Field(
			&l.Password,
			validation.Required.Error(l.RequiredError("Password")),
		),
		validation.Field(
			&l.ErrorMessage,
			l.IsValid(correctInfo).Error("Wrong email or password"),
		),
	)
}

type ChangePasswordForm struct {
	CurrentPassword string `json:"currentPassword"`
	NewPassword     string `json:"newPassword"`
	ConfirmPassword string `json:"confirmPassword"`
	Email           string

	formutil.FormValidation
}

func (c ChangePasswordForm) Validate() error {
	var isPasswordsSame bool
	var isCurrentPasswordCorrect bool
	var currentPassword string
	c.GetQuerier().QueryRow(
		`select password from user_profile where email = $1;`,
		c.Email).
		Scan(&currentPassword)

	if c.NewPassword == c.ConfirmPassword {
		isPasswordsSame = true
	} else {
		isPasswordsSame = false
	}

	//fmt.Printf("foo: %s", currentPassword)
	err := bcrypt.CompareHashAndPassword([]byte(currentPassword), []byte(c.CurrentPassword))

	if err != nil {
		isCurrentPasswordCorrect = false
	} else {
		isCurrentPasswordCorrect = true
	}

	c.CurrentPassword = formutil.StandardizeSpaces(c.CurrentPassword)
	c.NewPassword = formutil.StandardizeSpaces(c.NewPassword)
	c.ConfirmPassword = formutil.StandardizeSpaces(c.ConfirmPassword)

	return validation.ValidateStruct(
		&c,
		validation.Field(
			&c.CurrentPassword,
			validation.Required.Error(c.RequiredError("Current Password")),
			validation.Length(8, 0).Error("Current Password must be at least 8 characters"),
			c.IsValid(isCurrentPasswordCorrect).Error("Current Password not correct"),
		),
		validation.Field(
			&c.NewPassword,
			validation.Required.Error(c.RequiredError("New Password")),
			validation.Length(8, 0).Error("New Password must be at least 8 characters"),
		),
		validation.Field(
			&c.ConfirmPassword,
			validation.Required.Error(c.RequiredError("Confirm Password")),
			validation.Length(8, 0).Error("Confirm Password must be at least 8 characters"),
			c.IsValid(isPasswordsSame).Error("Passwords do not match"),
		),
	)
}

type ConfirmPasswordForm struct {
	NewPassword     string `json:"newPassword"`
	ConfirmPassword string `json:"confirmPassword"`
	formutil.FormValidation
}

func (c ConfirmPasswordForm) Validate() error {
	var isValid bool

	if c.NewPassword == c.ConfirmPassword {
		isValid = true
	} else {
		isValid = false
	}

	return validation.ValidateStruct(
		&c,
		validation.Field(
			&c.NewPassword,
			validation.Required.Error(c.RequiredError("New Password")),
			validation.Length(8, 0).Error("Password must be at least 8 characters"),
		),
		validation.Field(
			&c.ConfirmPassword,
			validation.Required.Error(c.RequiredError("Confirm Password")),
			validation.Length(8, 0).Error("Confirm Password must be at least 8 characters"),
			c.IsValid(isValid).Error("Passwords do not match"),
		),
	)
}
