peweproxy.register_module('reporer', function($) {
	
	var reporterButonSelector = 'div#peweproxy_icon_banner a.__peweproxy_reporter_button';
	
	var peweproxy_url_reporter = 'adaptive-proxy/reporter_call.html'
	
	var getReportedStatus = function(){
		var retVal;
		peweproxy.on_uid_ready(function(){
			retVal = $.ajax({
				async: false,
				url: peweproxy_url_reporter+"?action=getReportedStatus",
				data: {
					uid: peweproxy.uid
				},
				type: 'POST'
			}).responseText;
	          
			if (retVal == "true"){
				$('#peweproxy_statusFalse').addClass('hidden');
				$('#peweproxy_statusFalse').fadeOut('fast');
				$('#peweproxy_statusTrue').fadeIn('fast');
			}
		});
		return retVal;
	}
		
	$(document).ready(function(){
		getReportedStatus();
		$(reporterButonSelector).click(function(){
			$('#peweproxy_reporter').hide().removeClass('hidden').fadeIn('fast');
	              
			return false;
		});
		$('div#peweproxy_reporter a.__peweproxy_reporter_closebutton').click(function(){
			$(this).blur();
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
				
			peweproxy.on_uid_ready(function(){
				$.post(peweproxy_url_reporter+'?action=reportPage', {
					uid: peweproxy.uid
				});
				$('#peweproxy_statusFalse').fadeOut('fast');
				$('#peweproxy_statusTrue').fadeIn('fast');
			});
			return false;
		});
	});

});