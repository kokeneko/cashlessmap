$(document).ready(function() {
    var src = $("#rakutenicon").children('img').attr('src');
    $("#rakuten-selected").attr("src", src);
});

$('.menu-trigger').on('click',function(){
    if($(this).hasClass('active')){
        $(this).removeClass('active');
        $('nav').removeClass('open');
        $('.overlay').removeClass('open');
    } else {
        $(this).addClass('active');
        $('nav').addClass('open');
        $('.overlay').addClass('open');
    }
    });
    $('.overlay').on('click',function(){
    if($(this).hasClass('open')){
        $(this).removeClass('open');
        $('.menu-trigger').removeClass('active');
        $('nav').removeClass('open');      
    }
});