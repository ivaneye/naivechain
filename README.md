# naivechain
kotlin实现的区块链

参考：

- [A blockchain in 200 lines of code](https://medium.com/@lhartikk/a-blockchain-in-200-lines-of-code-963cc1cc0e54)需翻墙
- [A blockchain in 200 lines of code（翻译）](http://blog.jobbole.com/110860/)
- [js版naivechain](https://github.com/lhartikk/naivechain)
- [Java版naivechain(BlockService 73行判断错了)](https://github.com/sunysen/naivechain)


# 启动方式

启动参数添加 -Dserver.port=8080，启动不同的端口，模拟不同的节点。

假设目前启动了8080和8081两个端口：
- 通过POST http://localhost:8080/peer来添加节点的互通。body为peer=ws://localhost:8081/endpoint
- 添加完成后，8080与8081节点即建立了连接
- 通过POST http://localhost:8080/block来添加区块。body为data=11111