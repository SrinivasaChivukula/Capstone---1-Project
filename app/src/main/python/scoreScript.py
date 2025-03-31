import pandas as pd
import numpy as np
from sklearn.decomposition import PCA
from sklearn.preprocessing import StandardScaler
from sklearn.impute import SimpleImputer
from java.util import HashMap
def getScore(rKAMinL, rightKneeAngleMinList, rKAMaxL, rightKneeAngleMaxList, lKAMinL, leftKneeAngleMinList, lKAMaxL, leftKneeAngleMaxList, tAMinL, torsoAngleMinList, tAMaxL, torsoAngleMaxList):
     data_dict = {}

     data_dict[str(rKAMinL)] = list(rightKneeAngleMinList)
     data_dict[str(rKAMaxL)] = list(rightKneeAngleMaxList)
     data_dict[str(lKAMinL)] = list(leftKneeAngleMinList)
     data_dict[str(lKAMaxL)] = list(leftKneeAngleMaxList)
     data_dict[str(tAMinL)] = list(torsoAngleMinList)
     data_dict[str(tAMaxL)] = list(torsoAngleMaxList)

     # Find the maximum length among all lists
     max_len = max(len(v) for v in data_dict.values())

     # Pad shorter lists with NaN values
     for k, v in data_dict.items():
          data_dict[k] = v + [np.nan] * (max_len - len(v))

     df = pd.DataFrame(data_dict)
     df_numeric = df.select_dtypes(include=['number'])

     # Handle missing values by imputing with the mean
     imputer = SimpleImputer(strategy="mean")
     df_imputed = imputer.fit_transform(df_numeric)

     # Z-score normalization (standard scaling)
     scaler = StandardScaler()
     df_normalized = scaler.fit_transform(df_imputed)

     # Apply PCA with more components (e.g., 4 components)
     pca = PCA(n_components=4)  # Set n_components to 4 (or higher if needed)
     principal_components = pca.fit_transform(df_normalized)

     # DataFrame with principal components
     df_numeric["PC1"] = principal_components[:, 0]
     df_numeric["PC2"] = principal_components[:, 1]
     df_numeric["PC3"] = principal_components[:, 2]
     df_numeric["PC4"] = principal_components[:, 3]

     # Compute Gait Scores and Gait Index (you could use multiple components)
     gait_scores = df_numeric["PC1"] + df_numeric["PC2"] + df_numeric["PC3"] + df_numeric["PC4"]  # You can use a combination of PC1, PC2, etc. here
     gait_index = (gait_scores - gait_scores.min()) / (gait_scores.max() - gait_scores.min())
     df_numeric["Gait Index"] = gait_index

     # Mean Gait Index
     mean_gait_index = gait_index.mean()
     return mean_gait_index
