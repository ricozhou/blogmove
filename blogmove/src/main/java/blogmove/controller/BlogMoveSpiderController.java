package blogmove.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.websocket.api.WebSocketConstants;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import blogmove.config.FilePathConfig;
import blogmove.constant.BlogConstant;
import blogmove.constant.CommonSymbolicConstant;
import blogmove.domain.Blogcontent;
import blogmove.domain.Blogmove;
import blogmove.service.BlogMoveArticleService;
import blogmove.service.BlogMoveWordArticleService;
import blogmove.utils.BlogMoveCommonUtils;
import blogmove.utils.BlogMoveTouTiaoUtils;
import blogmove.utils.DateUtils;

/**
 * @author ricozhou
 * @date Oct 17, 2018 12:10:48 PM
 * @Desc
 */
public class BlogMoveSpiderController {
	BlogMoveArticleService blogMoveArticleService = new BlogMoveArticleService();
	BlogMoveWordArticleService blogMoveWordArticleService = new BlogMoveWordArticleService();
	public int num = 0;
	public StringBuilder sbmsg = new StringBuilder();

	public static void main(String[] args) {
		
		Blogmove blogMove = new Blogmove("今日头条", "50080767248", 50, 0, "默认", 1, 0, 0, 0, "https://www.toutiao.com/c/user/", 1,
				null,0);
		//去掉日志显示
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");  
		new BlogMoveSpiderController().blogMoveArticleController(blogMove);
	}

	/**
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws FailingHttpStatusCodeException
	 * @date Oct 17, 2018 12:11:53 PM
	 * @Desc 博客搬家启动方法
	 */
	public boolean blogMoveController(Blogmove blogMove) {
		String oneUrl;
		// 校验参数
		if (blogMove == null) {
			return false;
		}

		if (blogMove.getMoveMode() == 0) {
			blogMoveArticleController(blogMove);

		} else if (blogMove.getMoveMode() == 1) {
			blogMoveArticleController(blogMove);
		} else if (blogMove.getMoveMode() == 2) {
			// 读取word
			blogMoveWordController(blogMove);
		} else if (blogMove.getMoveMode() == 9) {

		}
		return false;
	}

	/**
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws FailingHttpStatusCodeException
	 * @throws InterruptedException
	 * @date Oct 17, 2018 12:11:53 PM
	 * @Desc CSDN博客搬家启动方法
	 */
	public boolean blogMoveArticleController(Blogmove blogMove) {
		String loginName = "superadmin";
		String statusMsg;

		String oneUrl = BlogMoveCommonUtils.getBlogMoveArticleListUrl(blogMove);

		// 校验参数
		if (blogMove == null) {
			return false;
		}
		// 首先获取文章列表url list
		List<String> urlList = new ArrayList<String>();
		int pageNum = BlogMoveCommonUtils.getBlogMoveArticlePageNum(blogMove);
		String webName = blogMove.getMoveWebsiteId();
		statusMsg = "-->> 正在获取" + webName + "文章列表URL...";
		sbmsg.append(blogMove.getMoveMessage() + "\n\r" + statusMsg);
		try {
			if (blogMove.getMoveMode() == 0) {
				String max_behot_time = "0";
				for (int i = 1; i < pageNum + 1; i++) {
					if (BlogConstant.BLOG_BLOGMOVE_WEBSITE_NAME_CSDN.equals(blogMove.getMoveWebsiteId())) {
						blogMoveArticleService.getCSDNArticleUrlList(blogMove, oneUrl + i, urlList);
					} else if (BlogConstant.BLOG_BLOGMOVE_WEBSITE_NAME_CNBLOG.equals(blogMove.getMoveWebsiteId())) {
						blogMoveArticleService.getCnBlogArticleUrlList(blogMove, oneUrl + i, urlList);
					} else if (BlogConstant.BLOG_BLOGMOVE_WEBSITE_NAME_TOUTIAO.equals(blogMove.getMoveWebsiteId())) {
						// 今日头条比较难爬取
						// 首先去获取三个参数as，cp，_signature这三个参数很重要，爬取首页则无所谓
						oneUrl = BlogMoveTouTiaoUtils.getTouTiaoListUrl(blogMove, i, max_behot_time);
						max_behot_time = blogMoveArticleService.getTouTiaoArticleUrlList(blogMove, oneUrl, urlList);
					} else if (BlogConstant.BLOG_BLOGMOVE_WEBSITE_NAME_JIANSHU.equals(blogMove.getMoveWebsiteId())) {
						blogMoveArticleService.getJianShuArticleUrlList(blogMove, oneUrl + i, urlList);
					} else if (BlogConstant.BLOG_BLOGMOVE_WEBSITE_NAME_OSCHINA.equals(blogMove.getMoveWebsiteId())) {
						blogMoveArticleService.getOsChinaArticleUrlList(blogMove, String.format(oneUrl, i), urlList);
					}

				}
			} else if (blogMove.getMoveMode() == 1) {
				urlList.clear();
				urlList.add(blogMove.getMoveWebsiteUrl());
			}

			statusMsg = "-->> " + webName + "文章列表URL获取完成...";
			sbmsg.append("\n\r" + statusMsg);
			// 根据url获取所有文章并插入数据库
			if (urlList == null || urlList.size() < 1) {
				statusMsg = "-->> 未发现" + webName + "文章...";
				sbmsg.append("\n\r" + statusMsg);
				statusMsg = "over";
				blogMove.setMoveMessage(sbmsg.toString());
				blogMove.setMoveSuccess(0);
				blogMove.setMoveStopMode(0);
				blogMove.setMoveSuccessNum(num);
				return true;
			}
			// 获取原有的文章
			List<Blogcontent> bList = new ArrayList<Blogcontent>();
			if (blogMove.getMoveRemoveRepeat() == 0) {
				// 获取原有文章列表
			}

			// 开始爬取
			Blogcontent blogcontent = new Blogcontent();

			for (String url : urlList) {
				if (BlogConstant.BLOG_BLOGMOVE_WEBSITE_NAME_CSDN.equals(blogMove.getMoveWebsiteId())) {
					blogcontent = blogMoveArticleService.getCSDNArticleMsg(blogMove, url, bList);
				} else if (BlogConstant.BLOG_BLOGMOVE_WEBSITE_NAME_CNBLOG.equals(blogMove.getMoveWebsiteId())) {
					blogcontent = blogMoveArticleService.getCnBlogArticleMsg(blogMove, url, bList);
				} else if (BlogConstant.BLOG_BLOGMOVE_WEBSITE_NAME_TOUTIAO.equals(blogMove.getMoveWebsiteId())) {
//					blogcontent = blogMoveArticleService.getTouTiaoArticleMsg(blogMove, url, bList);
				} else if (BlogConstant.BLOG_BLOGMOVE_WEBSITE_NAME_JIANSHU.equals(blogMove.getMoveWebsiteId())) {
					blogcontent = blogMoveArticleService.getJianShuArticleMsg(blogMove, url, bList);
				} else if (BlogConstant.BLOG_BLOGMOVE_WEBSITE_NAME_OSCHINA.equals(blogMove.getMoveWebsiteId())) {
					blogcontent = blogMoveArticleService.getOsChinaArticleMsg(blogMove, url, bList);
				}

				// 插入数据库
				if (blogcontent != null) {
					statusMsg = String.format(
							"-->> 正在爬取" + webName + "文章 -- <a href=\"%s\" target=\"_blank\">%s</a> -- %s -- %s", url,
							blogcontent.getTitle(),
							DateUtils.format(blogcontent.getGtmCreate(), DateUtils.YYYY_MM_DD_HH_MM_SS),
							blogcontent.getAuthor());
					sbmsg.append("\n\r" + statusMsg);
					statusMsg = String.format("-->> 正在存入数据库 -- <a href=\"%s\" target=\"_blank\">%s</a> -- %s -- %s",
							url, blogcontent.getTitle(),
							DateUtils.format(blogcontent.getGtmCreate(), DateUtils.YYYY_MM_DD_HH_MM_SS),
							blogcontent.getAuthor());
					sbmsg.append("\n\r" + statusMsg);
					num++;
				}
				// 延迟一秒
				Thread.sleep(1000);
			}
			statusMsg = "博客搬家完成";
			sbmsg.append("\n\r" + statusMsg);
			blogMove.setMoveSuccess(0);
			blogMove.setMoveStopMode(0);
			blogMove.setMoveSuccessNum(num);
		} catch (Exception e) {
			e.printStackTrace();
			statusMsg = "爬取文章出错";
			sbmsg.append("\n\r" + statusMsg);
			blogMove.setMoveSuccess(1);
			blogMove.setMoveStopMode(0);
			blogMove.setMoveSuccessNum(num);
		}
		statusMsg = "over";
		blogMove.setMoveMessage(blogMove.getMoveMessage() + "\n\r" + sbmsg.toString());
		return true;
	}

	/**
	 * @date Oct 29, 2018 2:34:48 PM
	 * @Desc 读取word
	 * @param blogMove
	 */
	private boolean blogMoveWordController(Blogmove blogMove) {
		String loginName = "superadmin";
		String statusMsg;

		// 校验参数
		if (blogMove == null) {
			return false;
		}
		statusMsg = "-->> 正在读取wWORD文档...";
		sbmsg.append(blogMove.getMoveMessage() + "\n\r" + statusMsg);

		if (blogMove.getMoveFileNames() == null
				|| CommonSymbolicConstant.EMPTY_STRING.equals(blogMove.getMoveFileNames())
				|| blogMove.getMoveFileNames().split(CommonSymbolicConstant.COMMA).length < 1) {
			statusMsg = "-->> 未发现WORD文档...";
			sbmsg.append("\n\r" + statusMsg);
			statusMsg = "over";
			blogMove.setMoveMessage(sbmsg.toString());
			blogMove.setMoveSuccess(0);
			blogMove.setMoveStopMode(0);
			blogMove.setMoveSuccessNum(num);
			return true;
		}

		// 获取原有的文章
		List<Blogcontent> bList = new ArrayList<Blogcontent>();
		if (blogMove.getMoveRemoveRepeat() == 0) {
			// 获取原有文章列表
		}
		try {
			String[] fileNames = blogMove.getMoveFileNames().split(CommonSymbolicConstant.COMMA);
			String[] fileONames = blogMove.getMoveFileONames().split(CommonSymbolicConstant.COMMA);
			Blogcontent blogcontent;
			for (int i = 0; i < fileNames.length; i++) {
				// 读取word并返回html字符串相关信息

				blogcontent = BlogMoveWordArticleService.getWordArticleMsg(blogMove, fileNames[i], fileONames[i],
						bList);

				// 插入数据库
				if (blogcontent != null) {
					statusMsg = String.format("-->> 正在读取WORD文档  --" + fileONames[i] + " ");
					sbmsg.append("\n\r" + statusMsg);
					statusMsg = String.format("-->> 正在存入数据库  --" + fileONames[i] + " ");
					sbmsg.append("\n\r" + statusMsg);
					num++;
				}
				// 延迟一秒
				Thread.sleep(1000);
			}
			statusMsg = "博客搬家完成";
			sbmsg.append("\n\r" + statusMsg);
			blogMove.setMoveSuccess(0);
			blogMove.setMoveStopMode(0);
			blogMove.setMoveSuccessNum(num);
		} catch (Exception e) {
			e.printStackTrace();
			statusMsg = "读取WORD出错出错";
			sbmsg.append("\n\r" + statusMsg);
			blogMove.setMoveSuccess(1);
			blogMove.setMoveStopMode(0);
			blogMove.setMoveSuccessNum(num);
		}
		statusMsg = "over";
		blogMove.setMoveMessage(blogMove.getMoveMessage() + "\n\r" + sbmsg.toString());
		// 最后操作
		return true;

	}

	public int getNum() {
		return num;
	}

	public String getSbmsg() {
		return sbmsg.toString();
	}

}
