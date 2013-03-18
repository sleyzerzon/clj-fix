{
  "name" : "test-market",
  "spec" : {
    "begin-string" : { 
      "tag" : "8",
      "transform-by" : "by-value",
      "values" : {
        "version" : "FIX.4.2"
      }
    },
    "body-length" : {
      "tag" : "9",
      "transform-by" : "to-int"
    },
    "msg-seq-num" : {
      "tag" : "34",
      "transform-by" : "to-int"
    },
    "msg-type" : {
      "tag" : "35",
      "transform-by" : "by-value",
      "values" : {
        "heartbeat" : "0",
        "test-request" : "1",
        "resend-request" : "2",
        "reject" : "3",
        "seq-reset" : "4",
        "logout" : "5",
        "indication-of-interest" : "6",
        "execution-report" : "8",
        "order-cancel-reject" : "9",
        "logon" : "A",
        "new-order-single" : "D",
        "order-cancel-request" : "F",
        "order-cancel-replace-request" : "G"
      }
    },
    "poss-dup-flag" : {
       "tag" : "43",
       "transform-by" : "by-value",
       "values" : {
         "yes" : "Y",
         "no" : "N"
       }
    },
    "sender-comp-id" : {
      "tag" : "49",
      "transform-by" : "to-string"
    },
    "sending-time" : {
      "tag" : "52",
      "transform-by" : "to-string"
    },
    "target-comp-id" : {
      "tag" : "56",
      "transform-by" : "to-string"
    },
    "poss-resend" : {
      "tag" : "97",
      "transform-by" : "by-value",
       "values" : {
         "yes" : "Y",
         "no" : "N"
       }
    },
    "orig-sending-time" : {
      "tag" : "122",
      "transform-by" : "to-string"
    },
    "on-behalf-of-comp-id" : {
      "tag" : "115",
      "transform-by" : "to-string"
    },
    "on-deliver-to-comp-id" : {
      "tag" : "128",
      "transform-by" : "to-string"
    },
    "checksum" : {
      "tag" : "10",
      "transform-by" : "to-string"
    },
    "encrypt-method" : {
      "tag" : "98",
      "transform-by" : "by-value",
      "values" : {
        "none" : 0
      }
    },
    "heartbeat-interval" : {
      "tag" : "108",
      "transform-by" : "to-int"
    },
    "reset-seq-num" : {
      "tag" :  "141",
      "transform-by" : "by-value",
      "values" : {
        "yes" : "Y",
        "no" : "N"
      }
    },
    "test-request-id" : {
      "tag" : "112",
      "transform-by" : "to-string"
    },
    "begin-seq-num" : {
      "tag" : "7",
      "transform-by" : "to-int"
    },
    "ending-seq-num" : {
      "tag" : "16",
      "transform-by" : "to-int"
    },
    "ref-seq-num" : {
      "tag" : "45",
      "transform-by" : "to-int"
    },
    "text" : {
      "tag" : "58",
      "transform-by" : "to-string"      
    },
    "ref-tag-id" : {
      "tag" : "371",
      "transform-by" : "to-int"
    },
    "ref-msg-type" : {
      "tag" : "372",
      "transform-by" : "to-string"
    },
    "session-reject-reason" : {
      "tag" : "373",
      "transform-by" : "to-int"
    },
    "new-seq-num" : {
      "tag" : "36",
      "transform-by" : "to-int"
    },
    "gap-fill-flag" : {
      "tag" : "123",
      "transform-by" : "by-value",
      "values" : {
        "yes" : "Y",
        "no" : "N"
      }
    },
    "account" : {
      "tag" : "1",
      "transform-by" : "to-string"
    },
    "client-order-id" : {
      "tag" : "11",
      "transform-by" : "to-string"
    },
    "currency" : {
      "tag" : "15",
      "transform-by" : "to-string"
    },
    "exec-inst" : {
      "tag" : "18",
      "transform-by" : "by-value",
      "values" : {
        "market-peg" : "P",
        "primary-peg" : "R",
        "mid-price peg" : "M"
      }
    },
    "hand-inst" : {
      "tag" : "21",
      "transform-by" : "by-value",
      "values" : {
        "private" : "1",
        "public" : "2",
        "manual" : "3"
      }
    },
    "id-source" : {
      "tag" : "22",
      "transform-by" : "to-string"
    },
    "ioi-id" : {
      "tag" : "23",
      "transform-by" : "to-string"
    },
    "order-qty" : {
      "tag" : "38",
      "transform-by" : "to-int"
    },
    "order-type" : {
      "tag" : "40",
      "transform-by" : "by-value",
      "values" : {
        "limit" : "2",
        "pegged" : "P"
      }
    },
    "price" : {
      "tag" : "44",
      "transform-by" : "to-double"
    },
    "rule-80a" : {
      "tag" : "47",
      "transform-by" : "by-value",
      "values" : {
        "agency" : "A",
        "principal" : "P"
      }
    },
    "security-id" : {
      "tag" : "48",
      "transform-by" : "to-string"
    },
    "side" : {
      "tag" :  "54",
      "transform-by" : "by-value",
      "values" : {
        "buy" : "1",
        "sell" : "2",
        "sell-short" : "5"
      }
    },
    "symbol" : {
      "tag" : "55",
      "transform-by" : "to-string"
    },
    "time-in-force" : {
      "tag" : "59",
      "transform-by" : "by-value",
      "values" : {
        "day" : "0",
        "ioc" : "3",
        "fok" : "4"
      }
    },
    "transact-time" : {
      "tag" : "60",
      "transform-by" : "to-string"
    },
    "ex-destination" : {
      "tag" : "100",
      "transform-by" : "by-value",
      "values" : {
        "route-to-quote" : "L"
      }
    },
    "min-qty" : {
      "tag" : "110",
      "transform-by" : "to-int"
    },
    "max-floor" : {
      "tag" : "111",
      "transform-by" : "to-int"
    },
    "locate-required" : {
      "tag" : "114",
      "transform-by" : "by-value",
      "values" : {
        "yes" : "Y",
        "no" : "N"
      }
    },
    "security-exchange" : {
      "tag" : "207",
      "transform-by" : "to-string"
    },
    "peg-difference" : {
      "tag" : "211",
      "transform-by" : "to-double"
    },
    "post-only" : {
      "tag" : "9303",
      "transform-by" : "by-value",
      "values" : {
        "yes" : "Y",
        "no" : "N"
      }
    },
    "order-visible" : {
      "tag" : "9479",
      "transform-by" : "by-value",
      "values" : {
        "yes" : "Y",
        "no" : "N"
      }
    },
    "ebbo" : {
      "tag" : "9500",
      "transform-by" : "by-value",
      "values" : {
        "yes" : "Y",
        "no" : "N"
      }
    },
    "bypass-marker" : {
      "tag" : "6791",
      "transform-by" : "by-value",
      "values" : {
        "yes" : "Y",
        "no" : "N"
      }
    },
    "suppress-self-trade" : {
      "tag" : "6783",
      "transform-by" : "by-value",
      "values" : {
        "yes" :  "Y",
        "no" : "N"
      }
    },
    "portfolio-id" : {
      "tag" : "6784",
      "transform-by" : "to-int"
    },
    "ioi-time" : {
      "tag" : "9505",
      "transform-by" : "to-int"
    },
    "min-fill-qty" : {
      "tag" : "9510",
      "transform-by" : "to-int"
    },
    "avg-price" : {
      "tag" : "6",
      "transform-by" : "to-double"
    },
    "cumulative-qty" : {
      "tag" : "14",
      "transform-by" : "to-int"
    },
    "exec-id" : {
      "tag" : "17",
      "transform-by" : "to-string"
    },
    "exec-ref-id" : {
      "tag" : "19",
      "transform-by" : "to-string"
    },
    "exec-trans-type" : {
      "tag" : "20",
      "transform-by" : "by-value",
      "values" : {
        "new" : "0",
        "cancel" : "1"
      }
    },
    "last-market" : {
      "tag" : "30",
      "transform-by" : "by-value",
      "values" : {
        "lit" : "79",
        "qlx" : "XQLX"
      }
    },
    "last-price" : {
      "tag" : "31",
      "transform-by" : "to-double"
    },
    "last-share" : {
      "tag" : "32",
      "transform-by" : "to-int"
    },
    "order-id" : {
      "tag" : "37",
      "transform-by" : "to-string"
    },
    "order-qty" : {
      "tag" : "38",
      "transform-by" : "to-int"
    },
    "order-status" : {
      "tag" : "39",
      "transform-by" : "by-value",
      "values" : {
        "pending-new" : "A",
        "new" : "0",
        "partial-fill" : "1",
        "filled" : "2",
        "canceled" : "4",
        "replace" : "5",
        "pending-cancel" : "6",
        "rejected" : "8",
        "pending-replace" : "E"
      }
    },
    "orig-client-order-id" : {
      "tag" : "41",
      "transform-by" : "to-string"
    },
    "order-reject-reason" : {
      "tag" : "103",
      "transform-by" : "to-int"
    },
    "exec-type" : {
      "tag" : "150",
      "transform-by" : "by-value",
      "values" : {
        "pending-new" : "A",
        "new" : "0",
        "partial-fill" : "1",
        "filled" : "2",
        "canceled" : "4",
        "replace" : "5",
        "pending-cancel" : "6",
        "rejected" : "8",
        "pending-replace" : "E"
      }
    },
    "leaves-qty" : {
      "tag" : "151",
      "transform-by" : "to-int"
    },
    "solicited-flag" : {
      "tag" : "377",
      "transform-by" : "by-value",
      "values" : {
        "yes" : "Y",
        "no" : "N"
      }
    },
    "clearing-exempt" : {
      "tag" : "6783",
      "transform-by" : "by-value",
      "values" : {
        "yes" : "Y",
        "no" : "N"
      }
    },
    "counter-party-portfolio-id" : {
      "tag" : "6794",
      "transform-by" : "to-int"
    },
    "post-only" : {
      "tag" : "9303",
      "transform-by" : "by-value",
      "values" : {
        "yes" : "Y",
        "no" : "N"
      }
    },
    "liquidity-flag" : {
      "tag" : "9730",
      "transform-by" : "by-value",
      "values" : {
        "added" : "A",
        "remove" : "R"
      }
    },
    "cancel-reject-reason" : {
      "tag" : "102",
      "transform-by" : "to-int"
    },
    "cancel-reject-response-type" : {
      "tag" : "434",
      "transform-by" : "by-value",
      "values" : {
        "order-cancel-request" : "1",
        "order-cancel-replace-request" : "2"
      }
    },
    "ioi-trans-type" : {
      "tag" : "28",
      "transform-by" : "by-value",
      "values" : {
        "new" : "N",
        "cancel" : "C"
      }
    },
    "ioi-ref-id" : {
      "tag" : "26",
      "transform-by" : "to-string"
    }
  },

  "tags-of-interest" : {
    "logon" : "49|108",
    "heartbeat" : "112",
    "test-request" : "112",
    "resend-request" : "7|16",
    "reject" : "45|58|371|372|373",
    "seq-reset" : "36|43|123",
    "logout" : "49|58",
    "indication-of-interest" : "52|23|28|55|26|9505",
    "execution-report" : "6|11|14|20|21|31|32|37|38|39|40|41|44|48|54|55|60|103|150|151|6783|9730",
    "order-cancel-reject" : "11|37|39|41|58|102|434"
  }
}
