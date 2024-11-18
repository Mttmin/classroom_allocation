import json
import matplotlib.pyplot as plt
import seaborn as sns  # Just import seaborn
import pandas as pd
import sys
import os

def load_data(filename):
    with open(filename, 'r') as f:
        return json.load(f)

def plot_room_utilization(data, output_dir):
    rooms_data = data['allocation']['rooms']
    
    # Create DataFrame
    df = pd.DataFrame(rooms_data)
    df['occupied'] = df['course'].notna()
    df['utilization'] = df.apply(
        lambda x: x['course']['size'] / x['capacity'] if x['course'] else 0, 
        axis=1
    )
    
    # Group by type
    by_type = df.groupby('type').agg({
        'occupied': 'mean',
        'utilization': 'mean'
    }).sort_values('occupied', ascending=True)
    
    # Plot
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 6))
    
    # Occupation rate
    sns.barplot(
        data=by_type.reset_index(),
        y='type',
        x='occupied',
        ax=ax1,
        palette='viridis'
    )
    ax1.set_title('Room Occupation Rate by Type')
    ax1.set_xlabel('Proportion of Rooms Occupied')
    ax1.set_ylabel('Room Type')
    
    # Utilization rate
    sns.barplot(
        data=by_type.reset_index(),
        y='type',
        x='utilization',
        ax=ax2,
        palette='viridis'
    )
    ax2.set_title('Average Room Utilization by Type')
    ax2.set_xlabel('Average Utilization (Size/Capacity)')
    ax2.set_ylabel('Room Type')
    
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, 'room_utilization.png'))
    plt.close()
    print(f"Generated room utilization plot")

def plot_allocation_statistics(data, output_dir):
    stats = data['statistics'][0]  # Taking first simulation result
    
    # Prepare data for choice distribution
    choice_dist = {int(k): v for k, v in stats['choiceDistribution'].items()}
    choices = range(1, max(choice_dist.keys()) + 1)
    frequencies = [choice_dist.get(i, 0) for i in choices]
    
    # Create DataFrame for seaborn
    df = pd.DataFrame({
        'Choice Number': list(choices),
        'Number of Courses': frequencies
    })
    
    # Plot
    plt.figure(figsize=(10, 6))
    sns.barplot(data=df, x='Choice Number', y='Number of Courses', color='skyblue')
    plt.title('Distribution of Allocation Choices')
    plt.grid(True, alpha=0.3)
    
    # Add key metrics as text
    metrics_text = (
        f"Allocation Rate: {stats['allocationRate']}%\n"
        f"First Choice Rate: {stats['firstChoiceRate']}%\n"
        f"Average Choice: {stats['averageChoice']}\n"
        f"Unallocated Rate: {stats['unallocatedRate']}%"
    )
    plt.text(0.95, 0.95, metrics_text,
             transform=plt.gca().transAxes,
             verticalalignment='top',
             horizontalalignment='right',
             bbox=dict(facecolor='white', alpha=0.8))
    
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, 'allocation_statistics.png'))
    plt.close()
    print(f"Generated allocation statistics plot")

def plot_room_size_distribution(data, output_dir):
    rooms_data = data['allocation']['rooms']
    df = pd.DataFrame(rooms_data)
    
    plt.figure(figsize=(12, 6))
    sns.boxplot(data=df, x='type', y='capacity', palette='viridis')
    plt.xticks(rotation=45, ha='right')
    plt.title('Room Capacity Distribution by Type')
    plt.xlabel('Room Type')
    plt.ylabel('Capacity')
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, 'room_sizes.png'))
    plt.close()
    print(f"Generated room size distribution plot")

def main():
    if len(sys.argv) < 2:
        print("Usage: python visualize.py <json_file_path>")
        sys.exit(1)
    
    json_path = sys.argv[1]
    output_dir = os.path.dirname(json_path)
    
    print(f"Loading data from {json_path}")
    
    try:
        # Set seaborn style
        sns.set_theme(style="whitegrid")  # Use seaborn's whitegrid style
        
        # Load data
        data = load_data(json_path)
        
        # Generate all plots
        plot_room_utilization(data, output_dir)
        plot_allocation_statistics(data, output_dir)
        plot_room_size_distribution(data, output_dir)
        
        print("Visualization completed successfully!")
        
    except Exception as e:
        print(f"Error during visualization: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()