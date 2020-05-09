V2meituan为v2多渠道打包实现
       1 Contents of ZIP entries（from offset 0 until the start of APK Signing Block）
       2 APK Signing Block
       3 ZIP Central Directory
       4 ZIP End of Central Directory
       采用签名2的特点，将压缩包分为4个区块，1 3 4设保护，修改区块2的数据，将渠道信息打包进去

V1360为v1多渠道打包
        利用zip文件添加comment
V1meituan为v1多渠道打包
        往zip包中添加目录META-INF/channel_渠道
V1meituan1为v1多渠道打包
        往zip包中添加目录META-INF/channel，然后将渠道信息写在这个文件中
Manifest为v1多渠道打包
        往AndroidManifest.xml文件中写入渠道信息在application节点下面添加meta-data节点