<h3>包含nutch1.6的代码和一些plugin</h3>
<h4>urlfilter-forbidden</h4>
extension point为org.apache.nutch.net.URLFilter
如果一个URL的任意字串在forbidden-urlfilter.txt中出现，在抓取时忽略这个url

<h4>parse-content</h4>
提取符合某种模式HTML页面的主要内容
