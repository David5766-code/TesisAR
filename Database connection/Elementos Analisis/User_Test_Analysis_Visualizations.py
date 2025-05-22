import sqlalchemy
import psycopg2
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from datetime import datetime
import numpy as np
from sqlalchemy import create_engine
import warnings


def create_db_engine():
    """Create SQLAlchemy engine for database connection"""
    try:
        engine = create_engine(
            'postgresql://postgres: @localhost:5432/user_tesis'
        )
        return engine
    except Exception as e:
        print(f"Error creating database engine: {e}")
        return None


def get_user_list():
    """Get list of available users"""
    engine = create_db_engine()
    if engine:
        try:
            query = "SELECT DISTINCT id_user, user_name FROM user_login"
            users_df = pd.read_sql_query(query, engine)
            return users_df.values.tolist()
        except Exception as e:
            print(f"Error fetching users: {e}")
    return []


def get_user_data(user_id):
    """Fetch data for specific user"""
    engine = create_db_engine()
    if engine:
        try:
            query = """
                SELECT ul.user_name, p.date_time_attempted, p.detection_time,
                       p.answer1, p.answer2, p.use_assistance
                FROM progress p
                JOIN user_login ul ON p.id_user = ul.id_user
                WHERE p.id_user = %(user_id)s
                ORDER BY p.date_time_attempted
            """
            df = pd.read_sql_query(query, engine, params={'user_id': user_id})

            # Convert boolean columns to proper boolean type
            bool_columns = ['answer1', 'answer2', 'use_assistance']
            for col in bool_columns:
                df[col] = df[col].astype(bool)

            return df
        except Exception as e:
            print(f"Error fetching user data: {e}")
    return None


def set_neon_style():
    """Set plot style to match neon theme"""
    plt.style.use('dark_background')
    colors = ['#FF1493', '#00FF00', '#00FFFF', '#FFD700', '#FF4500']

    # Improve plot readability
    plt.rcParams['axes.grid'] = True
    plt.rcParams['grid.alpha'] = 0.3
    plt.rcParams['grid.linestyle'] = '--'

    return colors


def create_visualizations(user_id):
    """Create all visualizations for a specific user"""
    df = get_user_data(user_id)
    if df is None or df.empty:
        print("No data available for this user")
        return

    colors = set_neon_style()
    username = df['user_name'].iloc[0]

    # Create figure with subplots
    fig = plt.figure(figsize=(20, 15))
    fig.suptitle(f'Performance Analysis for User: {username}',
                 color='white', size=20, pad=20)

    # 1. Line plot: Detection Time Trend
    ax1 = plt.subplot(2, 3, 1)
    plt.plot(df['date_time_attempted'], df['detection_time'],
             color=colors[0], linewidth=2, marker='o')
    plt.title('Detection Time Trend', color='white', pad=15)
    plt.xlabel('Attempt Date', color='white')
    plt.ylabel('Detection Time (seconds)', color='white')
    plt.xticks(rotation=45)

    # Add trend line
    z = np.polyfit(range(len(df)), df['detection_time'], 1)
    p = np.poly1d(z)
    plt.plot(df['date_time_attempted'], p(range(len(df))),
             '--', color='white', alpha=0.5)

    # 2. Bar plot: Performance Categories
    ax2 = plt.subplot(2, 3, 2)
    categories = []
    for _, row in df.iterrows():
        if not row['answer1'] and not row['answer2']:
            categories.append('Failed')
        elif (row['answer1'] and not row['answer2']) or (not row['answer1'] and row['answer2']):
            categories.append('Partial')
        else:
            categories.append('Complete')

    category_counts = pd.Series(categories).value_counts()
    bars = plt.bar(category_counts.index, category_counts.values,
                   color=colors[1:4], alpha=0.8, edgecolor='white')
    plt.title('Test Performance Distribution', color='white', pad=15)

    # Add value labels on bars
    for bar in bars:
        height = bar.get_height()
        plt.text(bar.get_x() + bar.get_width()/2., height,
                 f'{int(height)}',
                 ha='center', va='bottom', color='white')

    # 3. Pie chart: First Question Performance
    ax3 = plt.subplot(2, 3, 3)
    answer1_counts = df['answer1'].value_counts()
    plt.pie(answer1_counts.values,
            labels=['Correct', 'Incorrect'] if answer1_counts.index[0] else [
                'Incorrect', 'Correct'],
            colors=[colors[2], colors[3]],
            autopct='%1.1f%%',
            startangle=90,
            wedgeprops={'edgecolor': 'white'})
    plt.title('First Question Performance', color='white', pad=15)

    # 4. Pie chart: Second Question Performance
    ax4 = plt.subplot(2, 3, 4)
    answer2_counts = df['answer2'].value_counts()
    plt.pie(answer2_counts.values,
            labels=['Correct', 'Incorrect'] if answer2_counts.index[0] else [
                'Incorrect', 'Correct'],
            colors=[colors[2], colors[3]],
            autopct='%1.1f%%',
            startangle=90,
            wedgeprops={'edgecolor': 'white'})
    plt.title('Second Question Performance', color='white', pad=15)

    # 5. Pie chart: Assistance Usage
    ax5 = plt.subplot(2, 3, 5)
    assistance_counts = df['use_assistance'].value_counts()
    plt.pie(assistance_counts.values,
            labels=['Used', 'Not Used'] if assistance_counts.index[0] else [
                'Not Used', 'Used'],
            colors=[colors[1], colors[4]],
            autopct='%1.1f%%',
            startangle=90,
            wedgeprops={'edgecolor': 'white'})
    plt.title('Assistance Usage', color='white', pad=15)

    # Add performance summary
    ax6 = plt.subplot(2, 3, 6)
    ax6.axis('off')
    summary_text = (
        f"Performance Summary:\n\n"
        f"Total Attempts: {len(df)}\n"
        f"Average Detection Time: {df['detection_time'].mean():.2f}s\n"
        f"Best Detection Time: {df['detection_time'].min():.2f}s\n"
        f"Success Rate Q1: {(df['answer1'].sum() / len(df) * 100):.1f}%\n"
        f"Success Rate Q2: {(df['answer2'].sum() / len(df) * 100):.1f}%\n"
        f"Assistance Usage Rate: {(df['use_assistance'].sum() / len(df) * 100):.1f}%"
    )
    plt.text(0.1, 0.5, summary_text, color='white',
             fontsize=12, verticalalignment='center')

    plt.tight_layout(rect=[0, 0.03, 1, 0.95])
    return fig


def main():
    """Main function to run the visualization program"""
    users = get_user_list()
    if not users:
        print("No users found in database")
        return

    print("\nAvailable users:")
    for user_id, user_name in users:
        print(f"{user_id}: {user_name}")

    while True:
        try:
            user_id = input("\nEnter user ID to visualize (or 'q' to exit): ")
            if user_id.lower() == 'q':
                break

            user_id = int(user_id)
            if user_id in [u[0] for u in users]:
                fig = create_visualizations(user_id)
                if fig:
                    plt.show()
            else:
                print("Invalid user ID. Please try again.")
        except ValueError:
            print("Please enter a valid number.")
        except Exception as e:
            print(f"An error occurred: {e}")


if __name__ == "__main__":
    # Suppress warning about SQLAlchemy
    warnings.filterwarnings('ignore', category=UserWarning)
    main()
