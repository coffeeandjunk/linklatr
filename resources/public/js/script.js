$(document).ready(function(){

  function isUrlPresent(obj){
   return obj.url; 
  }

  function appendLink(obj, container){
    if(isUrlPresent){
      var link = '<div class="link-item">'+
        '<a class="link-title" href="'+ obj.url +'" target="_blank">' + obj.title + '</a>';
      container.append(link);
    }
  }
  
  var linksContainer = $('.links-container');
  $.ajax({
    type: "GET",
    url: '/links',
    success: function(response){
      if(response.length > 0){
        $.each(response, function(idx,obj){
            appendLink( obj, linksContainer )
          });
      }
      console.log(response);
    }
  });

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
            appendLink(response,linksContainer)
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
