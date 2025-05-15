require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const cors = require('cors');
const { pool, checkConnection } = require('./db.js');

const app = express();

app.use(express.json());
app.use(bodyParser.json());
app.use(cors({
    origin: process.env.CORS_ORIGIN,
    credentials: true
}));

const validateUserInput = (req, res, next) => {
    const { user_name, user_lastname, mail, age, password } = req.body;
    
    if (!mail?.match(/^\w+([.-_+]?\w+)*@\w+([.-]?\w+)*(\.\w{2,10})+$/)) {
        return res.status(400).json({ error: 'Invalid email format' });
    }
    
    if (password?.length < 8) {
        return res.status(400).json({ error: 'Password must be at least 8 characters long' });
    }
    
    if (age && (isNaN(age) || age < 0 || age > 120)) {
        return res.status(400).json({ error: 'Invalid age' });
    }
    
    next();
};

app.post('/signup', validateUserInput, async (req, res) => {
    const { user_name, user_lastname, mail, age, password_us } = req.body;
    
    try {
        const existingUser = await pool.query(
            'SELECT * FROM public.user_login WHERE mail = $1',
            [mail]
        );
        
        if (existingUser.rows.length > 0) {
            return res.status(409).json({ error: 'Email already registered' });
        }
        
        const hashedPassword = await bcrypt.hash(password_us, 9);
        
        const result = await pool.query(
            'INSERT INTO public.user_login (user_name, user_lastname, mail, age, password_us) VALUES ($1, $2, $3, $4, $5) RETURNING id_user',
            [user_name, user_lastname, mail, age, hashedPassword]
        );
        
        res.status(201).json({
            message: 'User registered successfully',
            userId: result.rows[0].id_user
        });
  
    } catch (error) {
        console.error('Registration error:', error);
        res.status(500).json({ error: 'Internal server error during registration' });
    }
});

app.post('/login', async (req, res) => {
    const { mail, password_us } = req.body;
    
    try {
        const result = await pool.query(
            'SELECT * FROM public.user_login WHERE mail = $1',
            [mail]
        );
        
        if (result.rows.length === 0) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }
        
        const user = result.rows[0];
        const isPasswordValid = await bcrypt.compare(password_us , user.password_us);
        
        if (!isPasswordValid) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }
        
        const token = jwt.sign(
            { id: user.id_user, email: user.mail },
            process.env.JWT_SECRET,
            { expiresIn: process.env.JWT_EXPIRES_IN }
        );
        
        res.json({
            token,
            user: {
                id: user.id_user,
                email: user.mail,
                name: user.user_name
            }
        });
    
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ error: 'Internal server error during login' });
    }
});

app.post('/progress', async (req, res) => {
    const { id_user, date_time_attempted, detection_time, use_assistance, answer1, answer2 } = req.body;
    
    try {
        const result = await pool.query(
            'INSERT INTO public.progress (id_user, date_time_attempted, detection_time, use_assistance, answer1, answer2) VALUES ($1, $2, $3, $4, $5, $6) RETURNING id_progress',
            [id_user, date_time_attempted, detection_time, use_assistance, answer1, answer2]
        );
        
        res.status(201).json({
            message: 'Progress saved successfully',
            progressId: result.rows[0].id_progress
        });
      
    } catch (error) {
        console.error('Error saving progress:', error);
        res.status(500).json({ error: 'Internal server error while saving progress' });
    }
});

app.get('/case', async (req, res) => {
    try {
        const result = await pool.query(
            'SELECT id_questions, practice_case, fault, component FROM public.questions ORDER BY RANDOM() LIMIT 1'
        );
        
        if (result.rows.length === 0) {
            return res.status(404).json({ error: 'No questions available' });
        }
        
        res.json(result.rows[0]);
        
    } catch (error) {
        console.error('Error fetching case:', error);
        res.status(500).json({ error: 'Internal server error while fetching case' });
    }
});

app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ error: 'Something broke!' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', async () => {
    console.log(`Server running on port ${PORT}`);
    await checkConnection();
});

process.on('SIGTERM', () => {
    console.info('SIGTERM signal received.');
    pool.end(() => {
        console.log('Database pool has ended');
        process.exit(0);
    });
});