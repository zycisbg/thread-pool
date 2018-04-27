# thread-pool
-- ---------------
<h5>在这里记录下线程池的实际应用场景。包括：<br>
ExecutorService 使用（包含CountDownLatch使用，和一个CyclicBarrier的demo）<br>
ForkJoinPool 使用  （包含RecursiveAction，RecursiveTask）<br>
spring的threadPool 使用<br>
-- ---------------
建表SQL

SET FOREIGN_KEY_CHECKS=0;
-- Table structure for tmp_user
DROP TABLE IF EXISTS `tmp_user`;
CREATE TABLE `tmp_user` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `tmp_message_record`;
CREATE TABLE `tmp_message_record` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `user_id` int(10) DEFAULT NULL,
  `user_phone` varchar(255) DEFAULT NULL,
  `message_content` varchar(255) DEFAULT NULL,
  `current_time_millis` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=556299 DEFAULT CHARSET=utf8;
