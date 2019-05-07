package com.model2.mvc.web.purchase;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.domain.Purchase;
import com.model2.mvc.service.domain.User;
import com.model2.mvc.service.product.ProductService;
import com.model2.mvc.service.purchase.PurchaseService;
import com.model2.mvc.service.user.UserService;

@Controller
@RequestMapping("/purchase/*")
public class PurchaseController {
	
	
	//Field
	@Autowired
	@Qualifier("purchaseServiceImpl")
	private PurchaseService purchaseService;

	@Autowired
	@Qualifier("userServiceImpl")
	private UserService userService;
	
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
//	
	public PurchaseController() {
		System.out.println(this.getClass());
	}
	
	@Value("#{commonProperties['pageUnit']}")
	//@Value("#{commonProperties['pageUnit'] ?: 3}")
	int pageUnit;
	
	@Value("#{commonProperties['pageSize']}")
	//@Value("#{commonProperties['pageSize'] ?: 2}")
	int pageSize;
	
	@RequestMapping(value="addPurchase", method=RequestMethod.GET)
	public ModelAndView addPurchase(@RequestParam("prodNo") int prodNo) 
			throws Exception{
		
		System.out.println("/purchase/addPurchaseView : GET");
		
		Product product= productService.getProduct(prodNo);

		ModelAndView modelAndView=new ModelAndView();
		modelAndView.addObject("product",product);
		modelAndView.setViewName("forward:/purchase/addPurchaseView.jsp");
		return modelAndView;
	}
	
	@RequestMapping(value="addPurchase", method=RequestMethod.POST)
	public ModelAndView addPurchase(@ModelAttribute("purchase") Purchase purchase, @ModelAttribute("product") Product product,
			@RequestParam("prodNo") int prodNo, Model model, @RequestParam("userId") String userId) throws Exception{
		
		System.out.println("/purchase/addPurchase : POST");
		
		productService.getProduct(prodNo);
		product.setProTranCode("1");
		
		User user=userService.getUser(userId);
		
		purchase.setPurchaseProd(product);
		purchase.setBuyer(user);
		purchase.setTranCode("1");
		
		purchaseService.addPurchase(purchase);
		
		
		System.out.println(purchase);
		
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.setViewName("forward:/purchase/addPurchaseAction.jsp");
		return modelAndView;
	}
	
	@RequestMapping(value="listPurchase")
	public ModelAndView getPurchaseList(@ModelAttribute("search") Search search, HttpServletRequest request)throws Exception{
		
		
		System.out.println("/purchase/listPurchase : GET / POST");
		
		HttpSession session = request.getSession();
		String userId =((User)session.getAttribute("user")).getUserId();
		
		System.out.println(userId);
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
			
		}
		search.setPageSize(pageSize);
		
		Map<String, Object> map =purchaseService.getPurchaseList(search,userId);	
		
		
		Page resultPage = new Page( search.getCurrentPage(), 
				((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println(resultPage);
		
		ModelAndView modelAndView=new ModelAndView();
		
		modelAndView.addObject("list", map.get("list"));
		modelAndView.addObject("resultPage", resultPage);
		modelAndView.addObject("search", search);
		modelAndView.addObject("userId", map.get("userId"));
		modelAndView.setViewName("forward:/purchase/listPurchase.jsp");
		
		return modelAndView;
	}
	
	@RequestMapping(value="getPurchase", method=RequestMethod.GET)
	public ModelAndView getPurchase2(@RequestParam("tranNo") int tranNo)
			throws Exception{
		
		System.out.println("/purchase/getPurchase2 : GET ");
		
		Purchase purchase = purchaseService.getPurchase2(tranNo);
			
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.setViewName("forward:/purchase/getPurchase.jsp?tranNo="+tranNo);
		modelAndView.addObject("purchase",purchase);
		
		return modelAndView;
		
	}
	

	@RequestMapping(value="updatePurchase", method=RequestMethod.GET)
	public ModelAndView updatePurchaseView(@RequestParam("tranNo") int tranNo, Model model )throws Exception{
		
		System.out.println("/purchase/updatePurchaseView : GET");
		
		Purchase purchase =purchaseService.getPurchase2(tranNo);
		
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.addObject("purchase", purchase);
		modelAndView.setViewName("forward:/purchase/updatePurchaseView.jsp");
		
		return modelAndView;
		
	}
	
	@RequestMapping(value="updatePurchase",method=RequestMethod.POST)
	public ModelAndView updatePurchase(@ModelAttribute("purchase") Purchase purchase)throws Exception{
		
		System.out.println("/purchase/updatePurchase : POST");
		
		purchaseService.updatePurchase(purchase);
		
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.addObject("purchase", purchase);
		modelAndView.setViewName("redirect:/purchase/getPurchase?tranNo="+purchase.getTranNo());
		return modelAndView;
	}
	
	
	@RequestMapping(value="updateTranCode",method=RequestMethod.GET)
	public ModelAndView updateTranCode(@ModelAttribute("purchase") Purchase purchase, 
			@RequestParam("tranNo") int tranNo, @RequestParam("tranCode") String tranCode)throws Exception{
		
		System.out.println("/purchase/updateTranCode : GET");
		
		purchase = purchaseService.getPurchase2(tranNo);
		purchase.setTranCode(tranCode);
		
		purchaseService.updateTranCode(purchase);
		
		System.out.println(purchase);
	
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.addObject("tranCode", tranCode);
		modelAndView.setViewName("redirect:/purchase/listPurchase");
		
		return modelAndView;
		
	}
	
	@RequestMapping(value="updateTranCodeByProd", method=RequestMethod.GET)
	public ModelAndView updateTranCodeByProd(	@ModelAttribute("purchase") Purchase purchase, @ModelAttribute("product") Product product,
			@RequestParam("prodNo") int prodNo, @RequestParam("proTranCode") String proTranCode)throws Exception{
		
		product = productService.getProduct(prodNo);
		System.out.println(product);
		product.setProTranCode(proTranCode);
		
		purchase = purchaseService.getPurchase(prodNo);
		System.out.println(purchase);
		purchase.setTranCode(proTranCode);
		
		System.out.println(proTranCode);
		
		
		purchaseService.updateTranCode(purchase);
		
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.addObject("proTranCode", proTranCode);
		modelAndView.addObject("product", product);
		modelAndView.addObject("purchase", purchase);
		
		modelAndView.setViewName("forward:/product/listProduct?prodNo="+prodNo+"&menu=manage");
		return modelAndView;
	}
	
}
