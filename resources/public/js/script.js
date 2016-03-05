
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
    self.limit = 8;
    self.offset = 0;
    //self.totalLinks = getTotalLinks();
    self.fetchLinkList({offset: self.offset, limit: self.limit, "initial-call": true}, self.handleLinklist);
    self.initPreviewModule();
    self.bindEvents();
    self.linkField = $('#link');
    self.linkField.focus();
  },
  fetchLinkList: function(params, callback){
    var self = LnkLtr;
    var linkLimit = params || {offset: self.offset, limit: self.limit};
    console.log(" linklimit: ", linkLimit, " params: ", params);
    return (LnkLtr.ajax('/links', linkLimit, callback)); 
  },
  handleLinklist: function(response){
    var self = LnkLtr;
    self.linkCount = response['link-count'];
    var resultSet = response['result-set'];
    self.offset += self.limit;
    if(resultSet){
      self.linkModule.displayList(resultSet);
    }
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
      self.find('.link-name').val(LnkLtr.utils.unescapeHtml(linkObj.title));
      self.find('.desc').val(LnkLtr.utils.unescapeHtml(linkObj.desc));
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
      //LnkLtr.link = response;
    }else{
      Lnkltr.handleError(response.error);  
    }
  },
  bindEvents: function(){
    var linkElm = document.getElementById('link');
    var self = this;
    //show linkpreview dialog on pressing enter ker
    linkElm.onkeypress = function(e){
      var code = (e.keyCode ? e.keyCode : e.which);
      if(code == 13) { //Enter keycode
        if(linkElm.value && LnkLtr.utils.isUrlValid(LnkLtr.utils.sanitizeUrl(linkElm.value))){
          //make a ajax call and get url details
          var url= '/link/details',
          data = { url: linkElm.value }; 
          LnkLtr.previewModal.modal('show');
          LnkLtr.ajax(url, data, self.handleGetLink);
        }
      }
    };
    //bind logout button action
    $('.logout-button').click(function(e){
      window.location.href = "/logout"
    });

      //bind delete action
    $('.collection-page').on('click','.link-container .link-footer a:first-child',function(e){ LnkLtr.deleteLink(e); })

    $(window).on('scroll', function(){
      if( $(window).scrollTop() == $(document).height() - $(window).height() ) {
        console.log('scroll is called');
        //self.fetchLinkList();
        console.log(" Offset: ", self.offset," LIMIT: ", self.limit, "Link Count: ", self.linkCount);
        if(self.offset < self.linkCount){ 
          self.fetchLinkList({offset: self.offset, limit: self.limit},function(res){
            self.linkModule.displayList(res['result-set'])
          });
          self.offset += self.limit;
        }
      }
    });
  },
  submitNewLink: function(formData){
    $.ajax({
      type: "POST",
      url: '/',
      data: formData,
      success: function( response ){
        //LnkLtr.linkModule.addNewLink(response);
        LnkLtr.linkField.val('');
        LnkLtr.linkModule.addNewLink(LnkLtr.linkModule.getLinkTemplate(), response);
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
  },
    deleteLink: function(e){
      console.log("from delteLink funciton e ", e);
      var linkItem =  $(e.target).closest('.link-item');
      var data = {linId:linkItem.data('link-id')};
      console.log(data);
      LnkLtr.linkModule.delteLink(linkItem.parent());
      $.ajax({
        type: "DELETE",
        url: '/link/' + linkItem.data('link-id'),
        data: data,
        success: function( response ){
          LnkLtr.linkModule.delteLink(linkItem);
        },
        error: function( response ){
          $(".error").text(response.responseJSON.data.error).show()
            .hide(4000);
        }
      });

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