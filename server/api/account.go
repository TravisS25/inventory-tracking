package api

import (
	"bytes"
	"database/sql"
	"encoding/json"
	"fmt"
	"net/http"
	"sort"
	"strconv"

	"github.com/TravisS25/httputil/formutil"

	"github.com/TravisS25/httputil/confutil"

	"github.com/TravisS25/inventory-tracking/src/server/models"

	"github.com/TravisS25/httputil"
	"github.com/TravisS25/httputil/apiutil"
	"github.com/TravisS25/httputil/cacheutil"
	"github.com/TravisS25/httputil/mailutil"
	"github.com/TravisS25/inventory-tracking/src/server/config"
	"github.com/TravisS25/inventory-tracking/src/server/forms"
	"github.com/TravisS25/inventory-tracking/src/server/helpers"
	"github.com/go-redis/redis"
	"github.com/gorilla/mux"
	"github.com/gorilla/sessions"
	"github.com/satori/go.uuid"

	"time"

	"golang.org/x/crypto/bcrypt"
)

type AccountAPI struct {
	db      httputil.DBInterface
	cache   cacheutil.CacheStore
	store   sessions.Store
	message mailutil.SendMessage
	formMap map[string]formutil.Validator
	isProd  bool
}

func NewAccountAPI(
	db httputil.DBInterface,
	cache cacheutil.CacheStore,
	store sessions.Store,
	message mailutil.SendMessage,
	isProd bool,
	formMap map[string]formutil.Validator,
) *AccountAPI {
	return &AccountAPI{
		db:      db,
		cache:   cache,
		store:   store,
		message: message,
		formMap: formMap,
		isProd:  isProd,
	}
}

// ------------------------------ APIS -------------------------------------

func (u *AccountAPI) AccountDetails(w http.ResponseWriter, r *http.Request) {
	user, _ := GetUser(apiutil.GetUser(r))
	groups := apiutil.GetUserGroups(r)
	apiutil.SendPayload(w, r, map[string]interface{}{
		"user":   user,
		"groups": groups,
	})
}

func (a *AccountAPI) Login(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {
		apiutil.SetToken(w, r)
		w.WriteHeader(http.StatusOK)
	} else if r.Method == "POST" {
		var form forms.LoginForm

		if apiutil.HasBodyError(w, r) {
			return
		}

		dec := json.NewDecoder(r.Body)
		err := dec.Decode(&form)

		if apiutil.HasDecodeError(w, err) {
			return
		}

		err = a.formMap["loginForm"].Validate(form)

		if apiutil.HasFormErrors(w, r, err) {
			return
		}

		user, err := models.QueryUserProfile(
			a.db,
			`
			select 
				user_profile.*
			from
				user_profile
			where 
				email = $1;`,
			form.Email,
		)

		if apiutil.HasQueryError(w, err, "User Not Found") {
			return
		}

		if !user.IsActive {
			w.WriteHeader(http.StatusConflict)
			w.Write([]byte("Account is not active"))
			return
		}

		userGroupJoinQuery :=
			`
			select
				user_group.name as "user_group.name"
			from
				user_group_join
			join
				user_group on user_group_join.user_group_id = user_group.id
			where
				user_group_join.user_profile_id = $1;
		`

		userGroupJoins, err := models.QueryUserGroupJoins(
			a.db,
			userGroupJoinQuery,
			user.ID,
		)

		if err != nil {
			apiutil.ServerError(w, err, "query user group join err")
			return
		}

		session, err := a.store.Get(r, "user")

		if err != nil {
			apiutil.ServerError(w, err, "user session err; login api")
			return
		}

		session.Values["user"], err = json.Marshal(user)

		if err != nil {
			apiutil.ServerError(w, err, "user marshal err; login api")
			return
		}

		var urlArray []string
		var groupArray []string
		for _, groupJoin := range userGroupJoins {
			groupArray = append(groupArray, groupJoin.UserGroup.Name)
			if val, ok := config.Routing[groupJoin.UserGroup.Name]; ok {
				urlArray = append(urlArray, val...)
			}
		}

		sort.Strings(urlArray)
		sort.Strings(groupArray)
		urlBytes, err := json.Marshal(urlArray)

		if err != nil {
			apiutil.ServerError(w, err, "url array marshal err; login api")
			return
		}

		groupBytes, err := json.Marshal(groupArray)

		if err != nil {
			apiutil.ServerError(w, err, "group array marshal err; login api")
			return
		}

		err = session.Save(r, w)

		if err != nil {
			apiutil.CheckError(err, "session save err; login api")
		}

		urlStringKey := fmt.Sprintf(confutil.URLKey, user.Email)
		groupStringKey := fmt.Sprintf(confutil.GroupKey, user.Email)
		a.cache.Set(urlStringKey, urlBytes, 0)
		a.cache.Set(groupStringKey, groupBytes, 0)
		w.Header().Set("id", strconv.Itoa(user.ID))
		w.Write([]byte("{}"))
	}
}

func (a *AccountAPI) Logout(w http.ResponseWriter, r *http.Request) {
	session, err := a.store.Get(r, "user")
	session.Values["user"] = nil
	err = session.Save(r, w)

	if err != nil {
		apiutil.ServerError(w, err, "")
		return
	}
}

func (a *AccountAPI) ChangePassword(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {
		apiutil.SetToken(w, r)
	} else if r.Method == "POST" {
		var form forms.ChangePasswordForm

		if apiutil.HasBodyError(w, r) {
			return
		}

		dec := json.NewDecoder(r.Body)
		err := dec.Decode(&form)

		if apiutil.HasDecodeError(w, err) {
			return
		}

		var user models.UserProfile
		err = json.Unmarshal(apiutil.GetUser(r), &user)

		if err != nil {
			apiutil.ServerError(w, err, "")
			return
		}

		form.Email = user.Email
		err = a.formMap["changePasswordForm"].Validate(form)

		if apiutil.HasFormErrors(w, r, err) {
			return
		}

		hash, err := bcrypt.GenerateFromPassword([]byte(form.NewPassword), bcrypt.DefaultCost)

		if err != nil {
			apiutil.ServerError(w, err, "")
			return
		}

		user.Password = string(hash)
		err = user.Update(a.db)

		if err != nil {
			apiutil.ServerError(w, err, "")
			return
		}
	}
}

func (a *AccountAPI) Verification(w http.ResponseWriter, r *http.Request) {
	param := mux.Vars(r)["token"]
	_, err := a.cache.Get(param)

	if err == redis.Nil {
		w.WriteHeader(http.StatusNotFound)
		return
	}

	a.cache.Del(param)
	w.WriteHeader(http.StatusOK)
}

func (a *AccountAPI) ResendVerification(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {
		apiutil.SetToken(w, r)
	} else if r.Method == "POST" {
		var form forms.EmailForm

		if apiutil.HasBodyError(w, r) {
			return
		}

		dec := json.NewDecoder(r.Body)
		err := dec.Decode(&form)

		if apiutil.HasDecodeError(w, err) {
			return
		}

		err = a.formMap["emailForm"].Validate(form)

		if apiutil.HasFormErrors(w, r, err) {
			return
		}

		query :=
			`
		select
			contractor.id,
			contractor.contractor_name,
			user_profile.full_name
		from
			user_profile
		join
			contractor on user_profile.contractor_id = contractor.id
		join
			customer on user_profile.customer_id = customer.id
		where
			user_profile.id = $1;
		`
		user, err := models.QueryUserProfile(a.db, query, form.Email)
		apiutil.CheckError(err, "")

		uuid4, _ := uuid.NewV4()
		token := uuid4.String()
		a.cache.Set(token, form.Email, ((time.Hour) * (24 * 7)))
		password := helpers.RandomString(10)

		var url string
		var fromEmail string

		if config.Conf.HTTPS {
			url += "https://"
		} else {
			url += "http://"
		}

		if config.Conf.EmailConfig.TestMode {
			fromEmail = config.Conf.EmailConfig.TestEmail.User
		} else {
			fromEmail = config.Conf.EmailConfig.LiveEmail.User
		}

		url += config.Conf.ClientDomain + "/account/verification/" + token + "/"

		var buf bytes.Buffer
		err = config.Template.ExecuteTemplate(
			&buf,
			"verification-email.html",
			map[string]interface{}{
				"toContact":    false,
				"user":         user,
				"url":          url,
				"tempPassword": password,
			},
		)

		if err != nil {
			apiutil.ServerError(w, err, "")
			return
		}

		err = mailutil.SendEmail(
			[]string{fromEmail},
			form.Email,
			"Account Verification",
			nil,
			buf.Bytes(),
			a.message,
		)

		if err != nil {
			apiutil.ServerError(w, err, "")
			return
		}
	}
}

func (a *AccountAPI) ResetPassword(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {
		apiutil.SetToken(w, r)
	} else if r.Method == "POST" {
		var form forms.EmailForm

		if apiutil.HasBodyError(w, r) {
			return
		}

		dec := json.NewDecoder(r.Body)
		err := dec.Decode(&form)

		if apiutil.HasDecodeError(w, err) {
			return
		}

		err = a.formMap["emailForm"].Validate(form)

		if apiutil.HasFormErrors(w, r, err) {
			return
		}

		v4, err := uuid.NewV4()

		if err != nil {
			apiutil.ServerError(w, err, "")
			return
		}

		hash := v4.String()
		cachedHours := 3
		var fromEmail string

		if config.Conf.EmailConfig.TestMode {
			fromEmail = config.Conf.EmailConfig.TestEmail.User
		} else {
			fromEmail = config.Conf.EmailConfig.LiveEmail.User
		}

		var buf bytes.Buffer
		err = config.Template.ExecuteTemplate(
			&buf,
			"reset-email.html",
			map[string]interface{}{
				"clientDomain": config.Conf.ClientDomain,
				"hash":         hash,
				"hours":        cachedHours,
			},
		)

		if err != nil {
			apiutil.ServerError(w, err, "")
			return
		}

		err = mailutil.SendEmail(
			[]string{fromEmail},
			form.Email,
			"Reset Password",
			nil,
			buf.Bytes(),
			a.message,
		)

		if err != nil {
			apiutil.ServerError(w, err, "mailError")
			return
		}

		a.cache.Set(hash, form.Email, (time.Hour * time.Duration(cachedHours)))

		// This is simply used for testing in non prod
		// Here we send the verification token in json response
		// so our tests can grab the token and test the confirm reset api
		if !config.Conf.Prod {
			apiutil.SendPayload(w, r, map[string]interface{}{
				"token": hash,
			})
		}
	}
}

func (a *AccountAPI) ConfirmPasswordReset(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {
		param := mux.Vars(r)["token"]
		_, err := a.cache.Get(param)

		if err != nil {
			w.WriteHeader(http.StatusNotFound)
			return
		}

		apiutil.SetToken(w, r)
	} else if r.Method == "POST" {
		param := mux.Vars(r)["token"]
		emailBytes, err := a.cache.Get(param)

		if err != nil {
			w.WriteHeader(http.StatusNotFound)
			return
		}

		email := string(emailBytes)

		var form forms.ConfirmPasswordForm
		dec := json.NewDecoder(r.Body)
		err = dec.Decode(&form)

		if err != nil {
			apiutil.ServerError(w, err, "")
			return
		}

		err = a.formMap["confirmPasswordForm"].Validate(form)

		if apiutil.HasFormErrors(w, r, err) {
			return
		}

		user, err := models.QueryUserProfile(
			a.db,
			"select * from user_profile where email = $1;",
			email,
		)

		fmt.Println("heeeere")
		if err == sql.ErrNoRows {
			apiutil.CheckError(err, "")
			w.WriteHeader(http.StatusNotFound)
			return
		} else if err != nil && err != sql.ErrNoRows {
			apiutil.ServerError(w, err, "")
			return
		}

		passwordBytes, err := bcrypt.GenerateFromPassword([]byte(form.ConfirmPassword), bcrypt.DefaultCost)

		if err != nil {
			apiutil.ServerError(w, err, "")
			return
		}

		user.Password = string(passwordBytes)
		err = user.Update(a.db)

		if err != nil {
			apiutil.ServerError(w, err, "")
			return
		}

		a.cache.Del(param)
		w.WriteHeader(http.StatusOK)
	}
}

func (a *AccountAPI) EmailExists(w http.ResponseWriter, r *http.Request) {
	email := mux.Vars(r)["email"]
	_, err := models.QueryUserProfile(
		a.db,
		"select id from user_profile where email = $1;",
		email,
	)
	apiutil.CheckError(err, "")

	if err == sql.ErrNoRows {
		fmt.Println("unique")
		apiutil.SendPayload(w, r, map[string]interface{}{
			"uniqueEmail": true,
		})
	} else {
		fmt.Println("not unique")
		apiutil.SendPayload(w, r, map[string]interface{}{
			"uniqueEmail": false,
		})
	}
}

// ------------------------------ QUERY APIS -------------------------------------

func (a *AccountAPI) LoggedIn(w http.ResponseWriter, r *http.Request) {
	user := apiutil.GetUser(r)
	fmt.Println(user)
	if user != nil {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("true"))
		return
	}

	w.WriteHeader(http.StatusNotFound)
	w.Write([]byte("false"))
}
