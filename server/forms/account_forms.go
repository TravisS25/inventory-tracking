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
// type EmailForm struct {
// 	Email string `json:"email"`
// 	formutil.FormValidation
// }

// func (e EmailForm) Validate() error {
// 	query := `select id from user_profile where email = $1;`
// 	exists := e.Exists(query, e.Email)

// 	return validation.ValidateStruct(
// 		&e,
// 		validation.Field(
// 			&e.Email,
// 			validation.Required.Error(e.RequiredError("Email")),
// 			e.IsValid(exists).Error(e.ExistError("Email")),
// 		),
// 	)
// }

type EmailForm struct {
	Email string `json:"email"`
}

func NewEmailValidator(f formutil.FormValidation) *EmailValidator {
	return &EmailValidator{
		FormValidation: f,
	}
}

type EmailValidator struct {
	formutil.FormValidation
}

func (e EmailValidator) Validate(item interface{}) error {
	form := item.(EmailForm)
	query := `select id from user_profile where email = $1;`
	exists := e.Exists(query, form.Email)

	return validation.ValidateStruct(
		&form,
		validation.Field(
			&form.Email,
			validation.Required.Error(e.RequiredError("Email")),
			e.IsValid(exists).Error(e.ExistError("Email")),
		),
	)
}

func NewLoginValidator(f formutil.FormValidation) *LoginValidator {
	return &LoginValidator{
		FormValidation: f,
	}
}

type LoginForm struct {
	Email        string `json:"email"`
	Password     string `json:"password"`
	ErrorMessage string `json:"errorMessage,omitempty"`
}

type LoginValidator struct {
	formutil.FormValidation
}

func (l LoginValidator) Validate(item interface{}) error {
	form := item.(LoginForm)

	var email string
	var password string
	correctInfo := true
	query := "select email, password from user_profile where email = $1;"
	err := l.GetQuerier().QueryRow(query, form.Email).Scan(&email, &password)

	if err != nil {
		fmt.Println(err)
	}

	if err == sql.ErrNoRows {
		fmt.Println(form.Email + "no here")
		correctInfo = false
	}

	fmt.Printf("email form: %s\n", form.Email)
	fmt.Printf("email: %s\n", email)
	fmt.Printf("password: %s\n", password)
	err = bcrypt.CompareHashAndPassword([]byte(password), []byte(form.Password))

	if err != nil {
		fmt.Printf("compare error: %s", err)
		correctInfo = false
	}

	return validation.ValidateStruct(
		&form,
		validation.Field(
			&form.Email,
			validation.Required.Error(l.RequiredError("Email")),
			is.Email.Error("Enter valid email"),
		),
		validation.Field(
			&form.Password,
			validation.Required.Error(l.RequiredError("Password")),
		),
		validation.Field(
			&form.ErrorMessage,
			l.IsValid(correctInfo).Error("Wrong email or password"),
		),
	)
}

// type ChangePasswordForm struct {
// 	CurrentPassword string `json:"currentPassword"`
// 	NewPassword     string `json:"newPassword"`
// 	ConfirmPassword string `json:"confirmPassword"`
// 	Email           string `json:"email"`

// 	formutil.FormValidation
// }

// func (c ChangePasswordForm) Validate() error {
// var isPasswordsSame bool
// var isCurrentPasswordCorrect bool
// var currentPassword string
// c.GetQuerier().QueryRow(
// 	`select password from user_profile where email = $1;`,
// 	c.Email).
// 	Scan(&currentPassword)

// if c.NewPassword == c.ConfirmPassword {
// 	isPasswordsSame = true
// } else {
// 	isPasswordsSame = false
// }

// //fmt.Printf("foo: %s", currentPassword)
// err := bcrypt.CompareHashAndPassword([]byte(currentPassword), []byte(c.CurrentPassword))

// if err != nil {
// 	isCurrentPasswordCorrect = false
// } else {
// 	isCurrentPasswordCorrect = true
// }

// c.CurrentPassword = formutil.StandardizeSpaces(c.CurrentPassword)
// c.NewPassword = formutil.StandardizeSpaces(c.NewPassword)
// c.ConfirmPassword = formutil.StandardizeSpaces(c.ConfirmPassword)

// return validation.ValidateStruct(
// 	&c,
// 	validation.Field(
// 		&c.CurrentPassword,
// 		validation.Required.Error(c.RequiredError("Current Password")),
// 		validation.Length(8, 0).Error("Current Password must be at least 8 characters"),
// 		c.IsValid(isCurrentPasswordCorrect).Error("Current Password not correct"),
// 	),
// 	validation.Field(
// 		&c.NewPassword,
// 		validation.Required.Error(c.RequiredError("New Password")),
// 		validation.Length(8, 0).Error("New Password must be at least 8 characters"),
// 	),
// 	validation.Field(
// 		&c.ConfirmPassword,
// 		validation.Required.Error(c.RequiredError("Confirm Password")),
// 		validation.Length(8, 0).Error("Confirm Password must be at least 8 characters"),
// 		c.IsValid(isPasswordsSame).Error("Passwords do not match"),
// 	),
// )
// }

type ChangePasswordForm struct {
	CurrentPassword string `json:"currentPassword"`
	NewPassword     string `json:"newPassword"`
	ConfirmPassword string `json:"confirmPassword"`
	Email           string `json:"email"`
}

func NewChangePasswordValidator(f formutil.FormValidation) *ChangePasswordValidator {
	return &ChangePasswordValidator{
		FormValidation: f,
	}
}

type ChangePasswordValidator struct {
	formutil.FormValidation
}

func (c ChangePasswordValidator) Validate(item interface{}) error {
	var isPasswordsSame bool
	var isCurrentPasswordCorrect bool
	var currentPassword string

	form := item.(ChangePasswordForm)

	c.GetQuerier().QueryRow(
		`select password from user_profile where email = $1;`,
		form.Email).
		Scan(&currentPassword)

	if form.NewPassword == form.ConfirmPassword {
		isPasswordsSame = true
	} else {
		isPasswordsSame = false
	}

	//fmt.Printf("foo: %s", currentPassword)
	err := bcrypt.CompareHashAndPassword([]byte(currentPassword), []byte(form.CurrentPassword))

	if err != nil {
		isCurrentPasswordCorrect = false
	} else {
		isCurrentPasswordCorrect = true
	}

	form.CurrentPassword = formutil.StandardizeSpaces(form.CurrentPassword)
	form.NewPassword = formutil.StandardizeSpaces(form.NewPassword)
	form.ConfirmPassword = formutil.StandardizeSpaces(form.ConfirmPassword)

	return validation.ValidateStruct(
		&form,
		validation.Field(
			&form.CurrentPassword,
			validation.Required.Error(c.RequiredError("Current Password")),
			validation.Length(8, 0).Error("Current Password must be at least 8 characters"),
			c.IsValid(isCurrentPasswordCorrect).Error("Current Password not correct"),
		),
		validation.Field(
			&form.NewPassword,
			validation.Required.Error(c.RequiredError("New Password")),
			validation.Length(8, 0).Error("New Password must be at least 8 characters"),
		),
		validation.Field(
			&form.ConfirmPassword,
			validation.Required.Error(c.RequiredError("Confirm Password")),
			validation.Length(8, 0).Error("Confirm Password must be at least 8 characters"),
			c.IsValid(isPasswordsSame).Error("Passwords do not match"),
		),
	)
}

// type ConfirmPasswordForm struct {
// 	NewPassword     string `json:"newPassword"`
// 	ConfirmPassword string `json:"confirmPassword"`
// 	formutil.FormValidation
// }

// func (c ConfirmPasswordForm) Validate() error {
// 	var isValid bool

// 	if c.NewPassword == c.ConfirmPassword {
// 		isValid = true
// 	} else {
// 		isValid = false
// 	}

// 	return validation.ValidateStruct(
// 		&c,
// 		validation.Field(
// 			&c.NewPassword,
// 			validation.Required.Error(c.RequiredError("New Password")),
// 			validation.Length(8, 0).Error("Password must be at least 8 characters"),
// 		),
// 		validation.Field(
// 			&c.ConfirmPassword,
// 			validation.Required.Error(c.RequiredError("Confirm Password")),
// 			validation.Length(8, 0).Error("Confirm Password must be at least 8 characters"),
// 			c.IsValid(isValid).Error("Passwords do not match"),
// 		),
// 	)
// }

type ConfirmPasswordForm struct {
	NewPassword     string `json:"newPassword"`
	ConfirmPassword string `json:"confirmPassword"`
}

func NewConfirmPasswordValidator(f formutil.FormValidation) *ConfirmPasswordValidator {
	return &ConfirmPasswordValidator{
		FormValidation: f,
	}
}

type ConfirmPasswordValidator struct {
	formutil.FormValidation
}

func (c ConfirmPasswordValidator) Validate(item interface{}) error {
	form := item.(ConfirmPasswordForm)

	var isValid bool

	if form.NewPassword == form.ConfirmPassword {
		isValid = true
	} else {
		isValid = false
	}

	return validation.ValidateStruct(
		&form,
		validation.Field(
			&form.NewPassword,
			validation.Required.Error(c.RequiredError("New Password")),
			validation.Length(8, 0).Error("Password must be at least 8 characters"),
		),
		validation.Field(
			&form.ConfirmPassword,
			validation.Required.Error(c.RequiredError("Confirm Password")),
			validation.Length(8, 0).Error("Confirm Password must be at least 8 characters"),
			c.IsValid(isValid).Error("Passwords do not match"),
		),
	)
}
