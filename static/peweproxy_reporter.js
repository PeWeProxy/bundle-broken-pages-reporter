
var renewSmallButton = true;
var reporterButonSelector = 'div#peweproxy_icon_banner a.__peweproxy_reporter_button';

var peweproxy_url_reporter = 'adaptive-proxy/reporter_call.html'

var temp = function($) {
    $(document).ready(function(){
      __ap_register_callback(function(){
      	reportedStatus = false;
          reportedStatus = getReportedStatus();
  
          
          
          $(reporterButonSelector).click(function(){
              //$(this).blur();
              //renewSmallButton = false;
          	$('#peweproxy_reporter').hide().removeClass('hidden').fadeIn('fast');
              
              return false;
          });
          $('div#peweproxy_reporter a.__peweproxy_reporter_closebutton').click(function(){
              $(this).blur();
              //renewSmallButton = true;
              $('#peweproxy_reporter').fadeOut('fast');
              return false;
          });
          
          	
  		$('a#peweproxy_reporter_cancel_button').click(function(){
              $(this).blur();
              renewSmallButton = true;
              
              $('#peweproxy_reporter').fadeOut('fast');
              return false;
          });
          
          $('a#peweproxy_reporter_confirm_button').click(function(){            
              $.post(peweproxy_url_reporter+'?action=reportPage', {
  			  uid: __peweproxy_uid
  			});
  			$('#peweproxy_statusFalse').fadeOut('fast');
  			$('#peweproxy_statusTrue').fadeIn('fast');
              return false;
          });
    
      });
    });
} (adaptiveProxyJQuery);

function getReportedStatus(){
	var retVal;
    var temp = function($) {
      __ap_register_callback(function(){
          retVal = $.ajax({
              async: false,
              url: peweproxy_url_reporter+"?action=getReportedStatus",
              data: {
                  uid: __peweproxy_uid
              },
              type: 'POST'
          }).responseText;
          
          if (retVal == "true"){
            $('#peweproxy_statusFalse').addClass('hidden');
            $('#peweproxy_statusFalse').fadeOut('fast');
            $('#peweproxy_statusTrue').fadeIn('fast');
          }
      });
    } (adaptiveProxyJQuery);
  return retVal;
}

function setShown(shown){
    var temp = function($) {
      __ap_register_callback(function(){
        shown = shown ? 1 : 0;
        $.post(peweproxy_url_reporter+'?action=setShown', {
            shown: shown,
            uid: __peweproxy_uid
        });
      });
    } (adaptiveProxyJQuery);
}
