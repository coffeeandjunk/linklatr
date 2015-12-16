var context = {link: {url: "https%3A%2F%2Fgithub.com%2Fring-clojure%2Fring-codec",
    user_id: 1,
      title: "This is supposed to be the url title",
			domain: "www.medium.com"
  },
  test: "njb"};


(function(global){
	var LnkLtr = global.LnkLtr;
	if(!LnkLtr){
		LnkLtr = {};
		global.LnkLtr = LnkLtr;
	}
})(this);

LnkLtr = {

  init: function(){
    var self = this;
    self.fetchLinkList(null,self.linkModule.displayList);
    //self.linkModule.displayList(list);
    self.bindEvents();
  },
  fetchLinkList: function(params, callback){
    /*var callbk = function(response){
      console.log("response>>>> ", response);
      return response;
    }*/
    return (LnkLtr.ajax('/links', null, callback)); 
  },
  showLoader: function(){
    $('.loader').removeClass('hide');
  },
  hideLoader: function(){
    $('.loader').addClass('hide')
  },
  handleGetLink: function(response){
    console.log('from handleGetLink::::   ', response);
    //TODO check of there is no error in the response
    LnkLtr.linkModule.addPreviewLink(response);
    LnkLtr.hideLoader();
  },
  bindEvents: function(){
    var linkElm = document.getElementById('link');
    var self = this;
    linkElm.onchange = function(e){
      if(linkElm.value && LnkLtr.utils.isUrlValid(LnkLtr.utils.sanitizeUrl(linkElm.value))){
        console.log(e.target.value, " is a valid url");
        //make a ajax call and get url details
        var url= '/link/details',
            data = { url: linkElm.value }; 
        LnkLtr.linkModule.resetImage();
        LnkLtr.showLoader();
        LnkLtr.ajax(url, data, self.handleGetLink);
      }
    };
  }

};


$(document).ready(function(){
  LnkLtr.init();

  function isUrlPresent(obj){
    return obj.url; 
  }

  // TODO remove this code
  //$('.dummy').click(function(){
    //LnkLtr.linkModule.addNewLink(context);
  //})

  function isFormValid(form){
    var linkTitle = form.find('.link-label-container');
    var link = form.find('.link-container');
    if(linkTitle.children('#link-name').val().length < 1 ){
      linkTitle.addClass('has-error'); 
      return false;
    }
    linkTitle.removeClass('has-error');
    if(link.children('#link').val().length < 1 ){
      link.addClass('has-error'); 
      return false;
    }
    link.removeClass('has-error');
    return true;
  }

  var form = $('#link-form');
  $('button.link-submit').click(function() {
    if(isFormValid(form)){
      console.log(form.serialize());
      $.ajax({
        type: "POST",
        url: '/',
        data: form.serialize(),
        success: function( response ){
          if(isUrlPresent(response)){
            LnkLtr.linkModule.addNewLink(response);
          }     
        },
        error: function( response ){

          $(".error").text(response.responseJSON.data.error).show()
            .hide(4000);
        }
      });
    }
  });
});