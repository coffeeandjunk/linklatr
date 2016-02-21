var context = {link: {url: "https%3A%2F%2Fgithub.com%2Fring-clojure%2Fring-codec",
    user_id: 1,
      title: "This is supposed to be the url title",
			domain: "www.medium.com"
  },
  test: "njb"};


/* (function(global){
	var LnkLtr = global.LnkLtr;
	if(!LnkLtr){
		LnkLtr = {};
		global.LnkLtr = LnkLtr;
	}
})(this); */

LnkLtr = {

  init: function(){
    var self = this;
    self.fetchLinkList(null,self.linkModule.displayList);
    self.initPreviewModule();
    //self.linkModule.displayList(list);
    self.bindEvents();
    self.linkField = $('#link');
    self.linkField.focus();
  },
  fetchLinkList: function(params, callback){
    /*var callbk = function(response){
      console.log("response>>>> ", response);
      return response;
    }*/
    return (LnkLtr.ajax('/links', null, callback)); 
  },
  initPreviewModule: function(){
    LnkLtr.previewModal =  $("#add-link-dlg").modal({
      onHide: function(){
        console.log('hidden');
        LnkLtr.previewModal.resetPreviewDialog();
      },
      onApprove: function() {
        console.log('Approve');
        //TODO if form submitted successfully the return true else throw error depending on the error type
        var form = $('#link-form'),
            formData = form.serialize(),
            submitData = LnkLtr.submitNewLink(formData);
      } 
    });
    var self = LnkLtr.previewModal;
    self.loader = function(arg){
      if(arg === 'show'){
        $(this).find('.loader-container').show();
      }else if(arg == 'hide'){
        $(this).find('.loader-container').hide();
      }else{
        console.log('Invalid arg for LnkLtr.previewModal.loader() funciton');
      }
    };
    self.loadDataPreviewDialog = function(linkObj){
      self.resetPreviewDialog();
      //TODO add image loader and callback to chack if image is available and laod the correct image
      self.setImage(linkObj.image_url);
      self.find('.link-name').val(linkObj.title);
      self.find('.desc').val(linkObj.desc);
      self.find('#link').val(linkObj.url);
      console.log(' load dialog called');
    };
    self.resetImage = function(){
      self.find('.link-preview-img').css('background-image', 'url("img/no-image.png")');
      self.find('#image-url').val('');
      self.loader('hide');
    };
    self.setImage = function(imgUrl){
      self.find('.link-preview-img').css('background-image', 'url(' + imgUrl + ')');
      self.find('#image-url').val(imgUrl);
      self.loader('hide');
    };
    self.resetPreviewDialog = function(){
      //self.find(".link-name").val('');
      //self.find(".desc").val('');
      self.find('#link-form')[0].reset();
      self.resetImage();
    };
  },
  handleError: function(error){
    var msg = error.msg || "";
    Lnkltr.uutils.showError(msg);
  },
  handleGetLink: function(response){
    console.log('from handleGetLink::::   ', response);
    //TODO check of there is no error in the response
    if(!response.error){
      LnkLtr.linkModule.addPreviewLink(response);
      LnkLtr.link = response;
    }else{
     Lnkltr.handleError(response.error);  
    }
  },
  bindEvents: function(){
    var linkElm = document.getElementById('link');
    var self = this;
    linkElm.onkeypress = function(e){
      var code = (e.keyCode ? e.keyCode : e.which);
      if(code == 13) { //Enter keycode
        
        if(linkElm.value && LnkLtr.utils.isUrlValid(LnkLtr.utils.sanitizeUrl(linkElm.value))){
          console.log(e.target.value, " is a valid url");
          //make a ajax call and get url details
          var url= '/link/details',
          data = { url: linkElm.value }; 
          LnkLtr.previewModal.modal('show');
          LnkLtr.ajax(url, data, self.handleGetLink);
        }
      }
    };
    $('.logout-button').click(function(e){
      window.location.href = "/logout"
    })
  },
  submitNewLink: function(formData){
    $.ajax({
      type: "POST",
      url: '/',
      data: formData,
      success: function( response ){
          //LnkLtr.linkModule.addNewLink(response);
          LnkLtr.linkField.val('');
          LnkLtr.linkModule.addNewLink(response);
      },
      error: function( response ){
        $(".error").text(response.responseJSON.data.error).show()
          .hide(4000);
      }
    });
  },

  isFormValid: function(formData){
    console.log('form is valid');
    return true;
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

});