# gmall
######-> 引用服务

gmall-user-web             8080<br />
gmall-user-service      用户服务   8070
<br />
gmall-manage-web        商品后台管理 8081<br />
gmall-manage-service    商品后台管理服务 8071
<br />
gmall-item-web          商品详情 8082<br />
gmall-item-service 
-> gmall-manage-service
<br />
gmall-search-web        搜索 8083<br />
gmall-search-service    搜索服务 8073
<br />
gmall-cart-web          购物车 8084<br />
gmall-cart-service      购物车服务 8074
<br />
gmall-passport-web      认证中心   8085<br />
-> gmall-user-service
<br />
gmall-order-web         订单 8086<br />
gmall-order-service     订单服务 8076
<br />
gmall-payment           支付系统 8087