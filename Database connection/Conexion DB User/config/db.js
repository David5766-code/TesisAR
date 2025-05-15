const { Pool } = require('pg');
require('dotenv').config();

const isProduction = process.env.NODE_ENV === 'production';

const poolConfig = {
   
    user: process.env.POSTGRES_USER,
    host: process.env.POSTGRES_HOST,
    database: process.env.POSTGRES_DB,
    password: process.env.POSTGRES_PASSWORD,
    port: parseInt(process.env.POSTGRES_PORT, 10),
   
    max: 20, 
    idleTimeoutMillis: 30000, 
    connectionTimeoutMillis: 2000,
    ssl: isProduction ? { rejectUnauthorized: false } : false
};

const pool = new Pool(poolConfig);

pool.on('error', (err, client) => {
    console.error('Unexpected error on idle client', err);
    process.exit(-1);
});

const checkConnection = async () => {
    try {
        const client = await pool.connect();
        console.log('Database connection successful');
        client.release();
    } catch (err) {
        console.error('Database connection error:', err);
    }
};

module.exports = {
    pool,
    checkConnection
};