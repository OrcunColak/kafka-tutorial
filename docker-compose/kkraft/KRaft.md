# Read Me

We need controller and brokers

# Environment Variables

All the variables are necessary and must be set.
KAFKA_PROCESS_ROLES
KAFKA_NODE_ID

KAFKA_LISTENERS : This is the address the broker will use to register itself as a listener. If this address is not
available this would throw an error.

KAFKA_CONTROLLER_QUORUM_VOTERS : Controllers register in the list of quorum voters.
E.g. "1@controller-1:29092,2@controller-2:29092,3@controller-3:29092"

KAFKA_CONTROLLER_LISTENER_NAMES
CLUSTER_ID