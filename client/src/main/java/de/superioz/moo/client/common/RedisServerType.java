package de.superioz.moo.client.common;

public enum RedisServerType {

    /**
     * Just one simple redis instance
     */
    SINGLE,

    /**
     * Shards data between instances (so-called hash slots which generates automatically)<br>
     *
     * @see <a href="https://redis.io/topics/cluster-tutorial">https://redis.io/topics/cluster-tutorial</a>
     */
    CLUSTER,

    /**
     * Same as {@link #REPLICATED}?
     */
    MASTER_SLAVE,

    /**
     * Redis slaves replicates the changes of the master<br>
     * Could be uses like reading only from the slaves and writing only into the master
     *
     * @see <a href="https://redis.io/topics/replication">https://redis.io/topics/replication</a>
     */
    REPLICATED,

    /**
     * Without human intervention, so mostly everything is handled automatically
     *
     * @see <a href="https://redis.io/topics/sentinel">https://redis.io/topics/sentinel</a>
     */
    SENTINEL

}
