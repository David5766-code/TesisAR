// src/index.js
const express = require('express');
const cors = require('cors');
const { Pool } = require('pg');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

const pool = new Pool({
    user: process.env.DB_USER,
    host: process.env.DB_HOST,
    database: process.env.DB_DATABASE,
    password: process.env.DB_PASSWORD,
    port: process.env.DB_PORT,
});

// Search symptoms route
app.post('/search/symptoms', async (req, res) => {
    let { symptoms } = req.body;

    try {
        if (!symptoms || !Array.isArray(symptoms) || symptoms.length === 0) {
            return res.status(400).json({ error: 'Se requiere al menos un síntoma o indicador válido en un array.' });
        }

        const normalizedSymptoms = symptoms.map(s => (typeof s === 'string' ? s.toLowerCase().trim() : null)).filter(s => s && s.length > 0);

        if (normalizedSymptoms.length === 0) {
            return res.status(400).json({ error: 'Todos los síntomas proporcionados son inválidos.' });
        }

        // Updated to use parameterized query for all symptoms
        const conditions = normalizedSymptoms.map((_, i) => 
            `(LOWER(p.symptom) LIKE $${i + 1} OR LOWER(p.indicator_lights) LIKE $${i + 1})`
        ).join(" OR ");

        const query = `
            SELECT DISTINCT 
                e.name_component, 
                f.fault_name, 
                f.description,
                p.symptom as symptom,
                p.id_failure as id_Failure
            FROM public.problems p
            JOIN public.engine e ON p.id_component = e.id_component
            JOIN public.faults f ON p.id_failure = f.id_failure
            WHERE ${conditions}
        `;

        const values = normalizedSymptoms.map(s => `%${s}%`);
        const result = await pool.query(query, values);

        // Map results to match SearchResult structure
        const mappedResults = result.rows.map(row => ({
            symptom: row.symptom,
            id_Failure: row.id_Failure,
            fault_name: row.fault_name,
            description: row.description,
            name_Component: row.name_component
        }));

        res.json(mappedResults);
    } catch (error) {
        console.error('Error en la búsqueda:', error);
        res.status(500).json({ error: 'Error interno en la búsqueda' });
    }
});


const PORT = process.env.PORT || 4000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
