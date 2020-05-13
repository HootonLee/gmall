$(".footer_foot .p1 a").hover(function(){
    $(this).css("color","#D70B1C")
},function(){
    $(this).css("color","#727272")
})

$(".footer .footer_center ol li a").hover(function(){
    $(this).css("color","#D70B1C")
},function(){
    $(this).css("color","#727272")
})


//购特色
$(".section_gts ol li").hover(function(){
    $(this).css("opacity","0.8")
},function(){
    $(this).css("opacity","1")
})
//还没逛够
$(".section_hmgg ul li").hover(function(){
    $(this).css("border","1px solid #f10214")
    $(this).children("p").eq(0).css("color","#c81623")
},function(){
    $(this).css("border","1px solid white")
    $(this).children("p").eq(0).css("color","#666666")
})
