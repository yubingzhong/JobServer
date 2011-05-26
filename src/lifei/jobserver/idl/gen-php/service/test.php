<?
$GLOBALS['THRIFT_ROOT'] = './thrift';
require_once $GLOBALS['THRIFT_ROOT'].'/Thrift.php';
require_once $GLOBALS['THRIFT_ROOT'].'/transport/TTransport.php';
require_once $GLOBALS['THRIFT_ROOT'].'/transport/TSocket.php';
require_once $GLOBALS['THRIFT_ROOT'].'/protocol/TBinaryProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'].'/transport/TFramedTransport.php';
require_once $GLOBALS['THRIFT_ROOT'].'/transport/TBufferedTransport.php';

require_once 'JobService.php';

$transport = new TSocket('127.0.0.1', 10086);
$transport->open();

$protocol = new TBinaryProtocol($transport);

$client= new JobServiceClient($protocol, $protocol);

$job = new Job();


$job->name = "全量抓取58时刻 - ".date('Y-m-d');
$job->desc = "全量抓取58时刻，由管理员 lifei 提交。";
$job->user = "lifei@kuxun.com";
$job->type = "58";
$job->command = '${System.getProperty("user.dir")}/jobs/58shike/job.sh';

$result = $client->submitJob($job);

echo $result;
$transport->close();
// vim600:ts=4 st=4 foldmethod=marker foldmarker=<<<,>>>
// vim600:syn=php commentstring=//%s
