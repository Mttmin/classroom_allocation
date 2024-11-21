import json
import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import sys
import os
from statistics import mean

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
    
    # Filter out GRANDS_AMPHIS
    df_filtered = df[df['type'] != 'GRANDS_AMPHIS']
    
    plt.figure(figsize=(12, 6))
    
    # Create boxplot
    ax = sns.boxplot(data=df_filtered, x='type', y='capacity', palette='viridis')
    
    # Add room counts above each box
    room_counts = df_filtered['type'].value_counts()
    y_max = df_filtered['capacity'].max()
    
    for i, type_name in enumerate(ax.get_xticklabels()):
        count = room_counts.get(type_name.get_text(), 0)
        plt.text(i, y_max * 1.05, f'n={count}', 
                horizontalalignment='center',
                fontsize=10,
                fontweight='bold')
    
    # Adjust layout to make room for counts
    plt.ylim(0, y_max * 1.15)  # Extend y limit to show counts
    plt.xticks(rotation=45, ha='right')
    plt.title('Room Capacity Distribution by Type\n(excluding Grands Amphis)')
    plt.xlabel('Room Type')
    plt.ylabel('Capacity')
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, 'room_sizes.png'))
    plt.close()
    print(f"Generated room size distribution plot")


def plot_strategy_comparisons(data, output_dir):
    stats = data['statistics']
    
    # Create DataFrame from statistics
    df_stats = pd.DataFrame(stats)
    
    # Group by strategy and calculate means
    strategy_means = df_stats.groupby('strategyName').agg({
        'allocationRate': lambda x: mean(float(str(v).replace(',', '.')) for v in x),
        'firstChoiceRate': lambda x: mean(float(str(v).replace(',', '.')) for v in x),
        'highRankRate': lambda x: mean(float(str(v).replace(',', '.')) for v in x),
        'averageChoice': lambda x: mean(float(str(v).replace(',', '.')) for v in x)
    }).round(2)
    
    # Sort by allocation rate
    strategy_means = strategy_means.sort_values('allocationRate', ascending=False)
    
    # Create figure with two subplots side by side
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 6))
    
    # Plot allocation rates
    bars1 = sns.barplot(
        y=strategy_means.index,
        x=strategy_means['allocationRate'],
        ax=ax1,
        palette='viridis'
    )
    ax1.set_title('Average Allocation Rate by Strategy')
    ax1.set_xlabel('Allocation Rate (%)')
    ax1.set_ylabel('Strategy')
    
    # Add value labels to bars
    for i, v in enumerate(strategy_means['allocationRate']):
        ax1.text(v + 1, i, f'{v:.1f}%', va='center')
    
    # Plot first choice rates
    bars2 = sns.barplot(
        y=strategy_means.index,
        x=strategy_means['firstChoiceRate'],
        ax=ax2,
        palette='viridis'
    )
    ax2.set_title('Average First Choice Rate by Strategy')
    ax2.set_xlabel('First Choice Rate (%)')
    ax2.set_ylabel('Strategy')
    
    # Add value labels to bars
    for i, v in enumerate(strategy_means['firstChoiceRate']):
        ax2.text(v + 1, i, f'{v:.1f}%', va='center')
    
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, 'strategy_comparison.png'))
    plt.close()
    print(f"Generated strategy comparison plot")

    # Create additional plot for more detailed metrics with dual y-axes
    fig, ax1 = plt.subplots(figsize=(10, 7))
    
    # Plot Average Choice on left y-axis
    color1 = '#2ecc71'  # Green
    ax1.set_xlabel('Strategy')
    ax1.set_ylabel('Average Choice', color=color1)
    bars1 = ax1.bar(range(len(strategy_means)), 
                   strategy_means['averageChoice'],
                   color=color1,
                   alpha=0.7,
                   label='Average Choice')
    ax1.tick_params(axis='y', labelcolor=color1)
    ax1.set_ylim(0, 5)  # Set y-axis limit

    # Add value labels to the first set of bars
    for idx, v in enumerate(strategy_means['averageChoice']):
        ax1.text(idx, v + 0.1, f'{v:.2f}', 
                ha='center', va='bottom', color=color1, fontweight='bold')
    
    # Create second y-axis for High Rank Rate
    ax2 = ax1.twinx()
    color2 = '#e74c3c'  # Red
    ax2.set_ylabel('High Rank Rate (%)', color=color2)
    
    # Plot High Rank Rate as line with points
    line = ax2.plot(range(len(strategy_means)), 
                   strategy_means['highRankRate'],
                   color=color2,
                   marker='o',
                   linewidth=2,
                   markersize=8,
                   label='High Rank Rate (%)')
    ax2.tick_params(axis='y', labelcolor=color2)
    
    # Add value labels to the points
    for idx, v in enumerate(strategy_means['highRankRate']):
        ax2.text(idx, v + 1, f'{v:.1f}%', 
                ha='center', va='bottom', color=color2, fontweight='bold')
    
    # Set x-axis labels
    # Set x-axis labels
    ax1.set_xticks(range(len(strategy_means)))
    ax1.set_xticklabels(strategy_means.index, rotation=45, ha='right')
    ax2.grid(False)
    ax1.grid(False)
    # Add title
    plt.title('Strategy Performance Metrics Comparison, lower is  better', pad=20)
    
    # Add legend
    lines1, labels1 = ax1.get_legend_handles_labels()
    lines2, labels2 = ax2.get_legend_handles_labels()
    ax1.legend(lines1 + lines2, labels1 + labels2, loc='upper right')
    
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, 'strategy_metrics.png'))
    plt.close()
    print(f"Generated additional strategy metrics plot")

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
        plot_strategy_comparisons(data, output_dir)
        
        print("Visualization completed successfully!")
        
    except Exception as e:
        print(f"Error during visualization: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()