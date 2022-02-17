#
# Diverging stacked bar chart generator supporting 5 and 7 pt. Likert scales
#
# Will grab any .csv file in the working dictionary and try to generate nicely looking diverging stacked bar chart out of them, automatically saved as high-dpi png, svg and pdf.
#
# (c) 2019 - Oliver Beren Kaul
#
# See data.csv for data format sample
#

import os
import pandas as pd
import numpy as np
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

xLabels5 = ["", "Trifft gar nicht zu", "Trifft eher nicht zu", "Neutral", "Trifft eher zu", "Trifft völlig zu"]
likertColors5 = ['white', '#EE6429', '#E4AF8E', '#999999', '#8FA6CA', "#00509B"]

def get_files(root, files_of_type):
    rv = []

    for cwd, folders, files in os.walk(root):
        for fname in files:
            if os.path.splitext(fname)[1] in files_of_type:
                print("adding file " + cwd + "/" + fname)
                # key = filename, value = directory of file
                rv.append(cwd + "/" + fname)

    return rv

    
def createDivergingStackedBarChart(title, filename, keys, data):
    #keys.reverse()
    #data.reverse()
    
    keys = np.array(keys)
    data = np.array(data).astype(float)
    
    print(keys)
    print(data)
    
    barLengths = data.sum(axis=1)
    maxBarLength = max(barLengths)*2 # can be fully on one side
    
    cutoff = 2
    
    leftSideSum = data[:,:cutoff].sum(axis=1)
    halfMiddles = data[:,int(len(data[0]) / 2)] * .5
    
    middles = leftSideSum + halfMiddles
    insertedInvisibleBars = np.abs(((maxBarLength/2)-middles))
    
    data = np.insert(data, 0, insertedInvisibleBars, axis=1)
    
    reorderedData = [[] for i in range(len(data[0]))]
    for i in range(len(reorderedData)):
        for j in range(len(data)):
            reorderedData[i].append(data[j][i])
    reorderedData = np.array(reorderedData)
    
    fig, ax = plt.subplots()
    #fig = plt.gcf()
    # base size 1.5mm + 0.8mm per q
    fig.set_size_inches(9, 0.8 + 0.4 * len(keys))   # 1.9, 9.6, 0.8 per q ; 2.3 + 0.8 per extra q
    
    topSubplots = 0.2/(0.9 + 0.4 * len(keys))
    fig.subplots_adjust(left=0.4, right=1.0, bottom=(3*topSubplots), top=(1-topSubplots))

    y_pos = np.arange(len(keys))
    lastBars = reorderedData[0] # shift initial bars to the right
    
    for i in range(1, len(reorderedData)):
        print("drawing bars for ", reorderedData[i], " with color " , likertColorsUsed[i])
        ax.barh(y_pos, reorderedData[i], left=lastBars, align='center', color=likertColorsUsed[i], zorder=4, edgecolor='white')
        
        for j in range(len(reorderedData[i])):
            if reorderedData[i][j] > 0:
                plt.text((lastBars[j] + reorderedData[i][j] / 2), y_pos[j], int(reorderedData[i][j]), zorder=5, color='white', horizontalalignment='center', verticalalignment='center')
        
        lastBars = lastBars + reorderedData[i]
    
    ax.set_yticks(y_pos)
    ax.set_yticklabels(keys)
    ax.invert_yaxis()
    
    plt.title(title, loc='left', x=-1.1, fontsize='large')

    for i in [1,2,3,4,5,6,7,8,9]:
        plt.axvline((maxBarLength / 10) * i, linestyle='--', color='black', alpha=.2, linewidth=0.75)


    # for i in range(len(keys)):
    #     zx = plt.axhline(i, linestyle='--', color='black', alpha=.2)
    
    plt.xlim(0, maxBarLength)

    xvalues = np.arange(maxBarLength / 10, maxBarLength, maxBarLength/5.0000001)
    xlabels = ["-80%", "-40%", "0%", "40%", "80%"]
    plt.xticks(xvalues, xlabels, fontsize='small')
    
    ax.spines['left'].set_visible(False)
    ax.spines['right'].set_visible(False)
    ax.spines['top'].set_visible(False)
    ax.spines['bottom'].set_visible(False)

    ax.tick_params(axis='both', which='both', bottom=False, right=False, left=False, top=False)
    ax.tick_params(axis='x', colors='#6B6B6B')

    patches = [mpatches.Patch(color=color, label=label) for color,label in zip(likertColorsUsed[1:], xLabelsUsed[1:])]
    plt.legend(handles=patches, loc='lower right', bbox_to_anchor=(0.97, -(0.5/(0.4 * len(keys)))), ncol=5, frameon=False, fontsize='small')
    
    
    # (handles=patches, loc='upper right', bbox_to_anchor=(0.97, yLegendPos), ncol=5, frameon=False) # fontsize=8

    #plt.tight_layout()
    #fig.savefig(filename + '.png', dpi=300)
    fig.savefig(filename + '.pdf')
    #fig.savefig(filename + '.svg')
    
    print("Done saving figures.")
    
    plt.show()
    
    


if __name__ == '__main__':

    possibleFiles = get_files(os.getcwd(), ".csv")

    print("found " + str(len(possibleFiles)) + " files.\n")

    allData = {}

    possibleFiles.sort()

    for fileName in possibleFiles:
        statements = []
        ratings = []
        justFileName = fileName.split("/")[-1].split(".")[0]
        if justFileName != "":
            title = ""
            with open(fileName) as fileOpened:
                first = 1
                for line in fileOpened:
                    if line.startswith("#") or first == 1:
                        first = 0
                        lineSplit = line.strip().split(";")
                        title = lineSplit[0]
                        continue
                    lineSplit = line.strip().split(";")
                    statements.append(lineSplit[0])
                    ratings.append(list(int(i) for i in lineSplit[1:]))
            
            if len(ratings[0]) == 5:
                likertScaleType = 5
            else:
                print("Likert scale not supported for the following number of ratings: ", len(ratings[0]))
            
            print("Detected Likert scales with", likertScaleType, "statements, according to first entry in data.")
            
            xLabelsUsed = xLabels5
            likertColorsUsed = likertColors5
                
            createDivergingStackedBarChart(title, "qualitativeFeedback-"+justFileName, statements, ratings)