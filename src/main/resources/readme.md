
## 测试步骤

### 1.前端需要设置（index.html）里面
endpoint: 'http://IP:8080/api/upload' 修改其中的IP
headers: {filePath: '/root/import'}  设置要上传的文件存放目录
     
### 2.application.yml中
app.tus-upload-directory=/nas/import/tus  修改为自己的目录 文件临时目录


### vpn下测试
### 下载性能测试
同时下载两个1g的文件：
5-8Mb/s

### 上传性能测试
单个：2G文件上传 
3 MB/S

同时上传两个文件：
1个20g  1个1g  8:52
1g： 3.2M/s
20g：



















