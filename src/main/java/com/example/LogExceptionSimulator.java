apiVersion: apps/v1
kind: Deployment
metadata:
  name: log-simulator
  namespace: default
  labels:
    app: log-simulator
spec:
  replicas: 1
  selector:
    matchLabels:
      app: log-simulator
  template:
    metadata:
      labels:
        app: log-simulator
        logging: "true"
    spec:
      containers:
        - name: log-simulator
          image: harbor.meta42.indc.vnet.com/library/log-simulator-java:TAG_NAME
          imagePullPolicy: IfNotPresent
          env:
            - name: ENV  # 发版环境
              value: 'deploy_env'
            - name: LANG
              value: "zh_CN.UTF-8"
            - name: TZ
              value: "Asia/Shanghai"  # 设置时区，避免日志时间错乱
            - name: JAVA_OPT
              value: '-Xms512m -Xmx2048m -Xmn256m -XX:+UseG1GC'
---
apiVersion: v1
kind: Service
metadata:
  name: log-simulator
  namespace: default
spec:
  selector:
    app: log-simulator
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
  type: NodePort
