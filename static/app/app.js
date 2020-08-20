
const Login = { template: '<log-in></log-in>' }
const Registration = { template: '<registration></registration>' }
const Profile = { template: '<profile></profile>' }
const AllUsers = {template : '<all-users> </all-users>'}
const AllReviews = {template : '<all-reviews> </all-reviews>'}
const Apartments = {template : '<apartments> </apartments>'}
const Reservation = {template : '<reservation> </reservation>'}



const router = new VueRouter({
    mode: 'hash',
    routes: [
   	  	{ path: '/', component: Apartments},
        { path: '/login', component: Login},
        { path: '/registration', component: Registration },
        { path: '/profile', component: Profile },
        { path: '/allUsers', component: AllUsers },
        { path: '/allReviews', component: AllReviews},
        { path: '/apartments', component: Apartments},
        { path: '/reservation', component: Reservation}
    ]
});

var app = new Vue({
    router,
    el: '#aplication',
    data : function () {
    	return {
     	 mode: 'NO_LOGIN'
    	}
  	},
    updated(){
         axios
          .get('/sesion')
          .then(response => (this.mode = response.data.typeOfUser))
    },
    methods : {
      logout : function(){
        axios.get('/logout')
        .then(function(response){
          if(response.data == true){
            this.mode= 'NO_LOGIN';
            alert("udjes ovde i mod: " + this.mode);
            window.location.href = "/#/login"; 
          }
        })

      }
    }

});