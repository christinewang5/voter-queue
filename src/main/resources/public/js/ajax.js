$(function() {
  $('#buttonSum').click(function(){
    var a = $('#a').val();
    var b = $('#b').val();

    $.ajax({
      url: '/api/sum?a=' + a + '&b='+b,
      success: function(data) {
        $('#result').text(data);
      }
    });
  });
});